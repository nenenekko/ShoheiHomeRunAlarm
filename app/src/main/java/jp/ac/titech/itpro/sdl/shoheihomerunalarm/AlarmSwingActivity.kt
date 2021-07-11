package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.content.ContentValues.TAG
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*


class AlarmSwingActivity : AppCompatActivity(), SensorEventListener, Runnable {
    private var homeruned: Boolean = false
    private var period = 0

    //mp3音源
    private var mp: MediaPlayer? = null



    //加速度表示
    private var manager: SensorManager? = null
    private var sensor: Sensor? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timer = Timer()
    private var gx = 0f
    private var gy = 0f
    private var gz = 0f
    private var rx = 0f
    private var ry = 0f
    private var rz = 0f
    private var vx = 0f
    private var vy = 0f
    private var vz = 0f
    private var accuracy = 0
    private var weighted_acceleration = 0.0
    private var moving_acceleration = 0.0
    private var homerun_bound = 20.0

    //タイマーが起動しているか否か
    private var shakeflag = false
    private val delay = SensorManager.SENSOR_DELAY_NORMAL
    private val type = Sensor.TYPE_ACCELEROMETER
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        val imageView: ImageView = findViewById(R.id.alarm_image_view)
        imageView.setBackgroundResource(R.drawable.bating)

        period = 100
        shakeflag = false

        //スクリーンロックを解除する
        //権限が必要
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //加速度計の用意
        manager = getSystemService(SENSOR_SERVICE) as SensorManager
        if (manager == null) {
            finish()
            return
        }
        sensor = manager!!.getDefaultSensor(type)
        if (sensor == null) {
            finish()
        }
    }

    public override fun onStart() {
        Log.d(TAG, "onStart in" + Thread.currentThread())
        super.onStart()

        //音を鳴らす
        if (mp == null) {
            //resのrawディレクトリにtest.mp3を置いてある
            mp = MediaPlayer.create(this, R.raw.test)

            //ループ設定
            mp!!.isLooping = true
        }
        mp!!.start()
    }

    public override fun onDestroy() {
        Log.d(TAG, "onDestroy in" + Thread.currentThread())
        super.onDestroy()
        stopAndRelease()
    }

    private fun stopAndRelease() {
        if (mp != null) {
            mp!!.stop()
            mp!!.release()
            //stopAlarm()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume in" + Thread.currentThread())
        super.onResume()
        manager!!.registerListener(this, sensor, delay)

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler?.post(this@AlarmSwingActivity)
            }
        }, 0, GRAPH_REFRESH_PERIOD_MS)

    }


    override fun onSensorChanged(event: SensorEvent) {

        //重力加速度の除去
        gx = ALPHA * gx + (1 - ALPHA) * event.values[0]
        gy = ALPHA * gy + (1 - ALPHA) * event.values[1]
        gz = ALPHA * gz + (1 - ALPHA) * event.values[2]
        rx = event.values[0] - gx
        ry = event.values[1] - gy
        rz = event.values[2] - gz
        Log.i(TAG, "x=$rx, y=$ry, z=$rz")
        weighted_acceleration = Math.sqrt((rx * rx + ry * ry + rz * rz).toDouble())
        Log.i(TAG, weighted_acceleration.toString())
        if (weighted_acceleration > homerun_bound) {
            homeruned = true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i(TAG, "onAccuracyChanged")
        this.accuracy = accuracy
    }

    override fun run() {
        //ユーザがホームランを打ったら
        if (homeruned) {
            Toast.makeText(applicationContext, "アラーム終了！", Toast.LENGTH_LONG).show()

            //mp音楽の停止
            mp!!.stop()

            //設定を初期化する
            initSetting()

            //300ms停止
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Toast.makeText(this, "take an error!!", Toast.LENGTH_LONG).show()
            }

            //設定画面に移る
            stopAndRelease()
            val intent = Intent(this@AlarmSwingActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun initSetting() {
        //センサーの停止
        manager!!.unregisterListener(this)

        //各フィールド変数を初期化
        homeruned = false
        weighted_acceleration = 0.0
        moving_acceleration = 0.0
        homerun_bound = 20.0
        shakeflag = false

    }

    companion object {
        //センサー関連
        private const val GRAPH_REFRESH_PERIOD_MS: Long = 20
        private const val ALPHA = 0.75f
        private val TAG = AlarmSwingActivity::class.java.simpleName
    }
}