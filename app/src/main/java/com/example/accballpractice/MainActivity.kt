package com.example.accballpractice

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

// SurfaceView生成時のイベントをコールバックで受け取るためにSurfaceHolder.Callbackを実装
class MainActivity : AppCompatActivity(), SensorEventListener, SurfaceHolder.Callback {

    // サーフェスビューの幅
    private var surfaceWidth: Int = 0
    // サーフェスビューの高さ
    private var surfaceHeight: Int = 0

    // ボールの半径
    private val radius = 50.0f
    // ボールの移動量を調整するための計数
    private val coef = 1000.0f

    // ボールの現在のx座標
    private var ballX: Float = 0f
    // ボールの現在のy座標
    private var ballY: Float = 0f
    // ボールのx方向への加速度
    private var vx: Float = 0f
    // ボールのy方向への加速度
    private var vy: Float = 0f
    // 前回センサーから加速度の値を受け取った時間を保持
    private var time: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
        指定した方向で画面を固定するために、requestedOrientationプロパティを利用
        SCREEN_ORIENTATION_PORTRAIT:縦方向に固定
         */
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_main)
        // holderプロパティを利用して、SurfaceHolderのインスタンスを取得
        val holder = surfaceView.holder
        // サーフェスビューが変更、もしくは破棄された時のイベントリスナー登録の為に、SurfaceHolderのaddCallbackメソッドを使用
        // 引数には、SurfaceHolder.Callbackを実装したクラスのインスタンスを指定。
        // 今回は、MainActivityに実装している為、thisで自身を指定
        holder.addCallback(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 空実装
    }

    /*
    引数・・・SensorEventクラスのインスタンス
    4つのプロパティ格納
    accuracy:センサーの精度
    sensor:イベントを起こしたSensorオブジェクト
    timestamp:イベントが起こった時刻
    values:センサーから得られた値を格納した配列
    valuesに格納される値はセンサーごとに異なる。加速度センサーの場合は
    values[0]:x軸方向の加速度(float型)
    values[1]:y軸方向の加速度(float型)
    values[2]:z軸方向の加速度(float型)
     */
    override fun onSensorChanged(event: SensorEvent?) {
        // 引数のSensorEventクラスのインスタンスがnullの場合は何もしない
        if (event == null) return
        // 現在の時間をミリ秒で取得
        if (time == 0L) time = System.currentTimeMillis()
        /*
        センサーのタイプが加速度センサーかどうかチェック
        sensor.typeプロパティで、引数のSensorEventクラスのインスタンスのタイプを確認
         */
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // センサーから取得した、x,y方向の加速度データをそれぞれ代入。x軸の値は、画面の描画方向に合わせる為反転
            val x = -event.values[0]
            val y = event.values[1]

            // 現在時間から前回センサーの値を取得した時間を引いた、経過時間を取得
            var t = (System.currentTimeMillis() - time).toFloat()
            time = System.currentTimeMillis()
            // 経過時間をミリ秒→秒単位に変更
            t /= 1000.0f

            val dx = vx * t + x * t * t / 2.0f
            val dy = vy * t + y * t * t / 2.0f
            ballX += dx * coef
            ballY += dy * coef
            vx += x * t
            vy += y * t

            if (ballX - radius < 0 && vx < 0) {
                vx = -vx / 1.5f
                ballX = radius
            } else if (ballX + radius > surfaceWidth && vx > 0) {
                vx = -vx / 1.5f
                ballX = surfaceWidth - radius
            }
            if (ballY - radius < 0 && vy < 0){
                vy = -vy / 1.5f
                ballY = radius
            } else if (ballY + radius > surfaceHeight && vy > 0) {
                vy = -vy / 1.5f
                ballY = surfaceHeight - radius
            }

            drawCanvas()
        }
    }

    private fun drawCanvas() {
        // 描画するサーフェスの画面に対して、SurfaceHolderのlockCanvasメソッドを使用して、Canvasクラスのインスタンスを取得
        // これをすることで、描画するサーフェスがロックされ、描画可能な状態となる
        val canvas = surfaceView.holder.lockCanvas()
        // drawColorメソッドで、キャンバス全体の背景を指定した色で塗りつぶす
        canvas.drawColor(Color.YELLOW)
        /*
        drawCircleメソッドで、円を描く
        第一引数・・・円の中心X座標
        第二引数・・・円の中心Y座標
        第三引数・・・円の半径
        第四引数・・・描画に用いるPaintクラスのインスタンス
        ※第四引数のPaintクラスのインスタンスはその場で作成
        スコープ関数applyを使用することで、Paintクラスのcolorプロパティに、Color.MAGENTAを格納した状態の
        Paintクラスのインスタンスを生成してる
         */
        canvas.drawCircle(ballX, ballY, radius, Paint().apply {
            color = Color.MAGENTA
        })
        // 描画完了後、サーフェスのアンロック
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    /*
    サーフェスが変更された時に実行する処理を定義
    holder:変更が発生したサーフェスのSurfaceHolderオブジェクトが渡される
    format:サーフェス内の、各ピクセルのカラーデータ形式が渡される
    width:サーフェスの幅
    height:サーフェスの高さ
     */
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        surfaceWidth = width
        surfaceHeight = height
        // 幅と高さの半分を取得することで、ボールの初期位置を画面中央に設定
        ballX = (width / 2).toFloat()
        ballY = (height / 2).toFloat()
    }

    /*
    サーフェスが破棄される直前に実行する処理を定義
    引数・・・削除されるサーフェスのSurfaceHolderオブジェクトが渡される
     */
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    /*
    サーフェスが最初に作成された後に実行する処理を定義
    引数・・・作成されたサーフェスのSurfaceHolderオブジェクトが渡される
     */
    override fun surfaceCreated(holder: SurfaceHolder?) {
        // getSystemServiceメソッドの戻り値はObject型の為、as演算子でSensorManager型にキャスト
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // SensorManagerのgetDefaultSensorメソッドによりSensorクラスのインスタンスを取得
        // 引数にはセンサーの種類を定数で指定。TYPE_ACCELEROMETER・・・加速度センサー
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // SensorManagerのregisterListenerメソッドで、コールバックを受け取るリスナーを指定
        // 第一引数・・・SensorEventListenerインタフェースを実装したクラスのインスタンス。今回はMainActivityクラスに実装している為、thisで自身を指定
        // 第二引数・・・監視する対象のSensorオブジェクト
        // 第三引数・・・センサーの更新頻度。SENSOR_DELAY_GAME・・・約3-30ms
        sensorManager.registerListener(
            this, accSensor,
            SensorManager.SENSOR_DELAY_GAME
        )
    }
}