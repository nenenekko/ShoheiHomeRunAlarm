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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_alarm.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*


class AlarmNortificationActivity : AppCompatActivity(), SensorEventListener, Runnable {
    //タイマー関連

    private val timerhandler = Handler()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            count++
            timerText!!.text = dataFormat.format((shakeTime * 10 - count) * period)
            timerhandler.postDelayed(this, period.toLong())
        }
    }


    private var timerText: TextView? = null
    private val dataFormat = SimpleDateFormat("mm:ss.S", Locale.US)
    private var count = 0
    private var period = 0

    //シェイクゲージ
    private var bar: ProgressBar? = null

    //mp3音源
    private var mp: MediaPlayer? = null



    //加速度表示
    private var infoView: TextView? = null
    private var timeView: TextView? = null
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
    private val N = 25
    private val ax = FloatArray(N)
    private val ay = FloatArray(N)
    private val az = FloatArray(N)
    private var index = 0
    private var sx = 0f
    private var sy = 0f
    private var sz = 0f
    private var wx = 0f
    private var wy = 0f
    private var wz = 0f
    private var rate = 0
    private var accuracy = 0
    private var weighted_acceleration = 0.0
    private var moving_acceleration = 0.0
    private var accelerationMAX = 0.0
    private var prevTimestamp: Long = 0

    //タイマーが起動しているか否か
    private var shakeflag = false
    private val delay = SensorManager.SENSOR_DELAY_NORMAL
    private val type = Sensor.TYPE_ACCELEROMETER
    public override fun onCreate(savedInstanceState: Bundle?) {
        //Log.d(TAG, "onCreate in" + Thread.currentThread())
        Log.d("status", "画面2だよ")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        count = 0
        period = 100
        shakeflag = false
        timerText = findViewById(R.id.timer)
        timerText?.setText(dataFormat.format(shakeTime * 10 * period))
        time_view.text = getString(R.string.time_format, shakeTime)

        bar = findViewById(R.id.progressBar)
        bar?.setMax(100)
        bar?.setProgress(0)

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
        //Toast.makeText(applicationContext, "アラーム！", Toast.LENGTH_LONG).show()

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
                handler?.post(this@AlarmNortificationActivity)
            }
        }, 0, GRAPH_REFRESH_PERIOD_MS)

    }

    private fun stopAlarm() {
        initSetting()
        //stopAndRelease()
        val intent = Intent(this@AlarmNortificationActivity, MainActivity::class.java)
        startActivity(intent)
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

        //移動平均の計算
        sx = sx - ax[index] + rx
        ax[index] = rx
        wx = sx / N
        sy = sy - ay[index] + ry
        ay[index] = ry
        wy = sy / N
        sz = sz - az[index] + rz
        az[index] = rz
        wz = sz / N
        index = (index + 1) % N

        //重み付き平均の計算
        vx = ALPHA * vx + (1 - ALPHA) * rx
        vy = ALPHA * vy + (1 - ALPHA) * ry
        vz = ALPHA * vz + (1 - ALPHA) * rz
        val ts = event.timestamp
        rate = (ts - prevTimestamp).toInt() / 1000
        prevTimestamp = ts

        //android端末の加速度の計算、最大加速度の記憶
        weighted_acceleration = Math.sqrt((vx * vx + vy * vy + vz * vz).toDouble()) * 100
        moving_acceleration = Math.sqrt((wx * wx + wy * wy + wz * wz).toDouble()) * 100
        if (weighted_acceleration > accelerationMAX) {
            accelerationMAX = weighted_acceleration
        }

        //プログレスバーを更新
        if (moving_acceleration > 500) {
            bar!!.progress = 100
        } else if (moving_acceleration > 100) {
            bar!!.progress = 50 + (moving_acceleration - 100).toInt() / 8
        } else if (moving_acceleration > 30) {
            bar!!.progress = 25 + (moving_acceleration - 30).toInt() * 25 / 70
        } else {
            bar!!.progress = (moving_acceleration * 25 / 30).toInt()
        }

        //重み付き平均加速度が500を超えたらタイマー起動
        if (weighted_acceleration >= 400 && moving_acceleration >= 100 && !shakeflag) {
            timerhandler.post(runnable)
            shakeflag = true
            //移動平均加速度が100を下回ったらタイマー停止
        } else if (moving_acceleration < 30 && shakeflag) {
            timerhandler.removeCallbacks(runnable)
            timerText!!.text = dataFormat.format(shakeTime * 10 * period)
            count = 0
            shakeflag = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i(TAG, "onAccuracyChanged")
        this.accuracy = accuracy
    }

    override fun run() {
        infoView?.text = getString(R.string.info_format, moving_acceleration)

        //タイマーが6秒を超えたら
        if (count >= shakeTime * 10) {
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
            val intent = Intent(this@AlarmNortificationActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun initSetting() {
        //センサーの停止
        manager!!.unregisterListener(this)

        //各フィールド変数を初期化
        count = 0
        weighted_acceleration = 0.0
        moving_acceleration = 0.0
        accelerationMAX = 0.0
        shakeflag = false

        //タイマーを初期化
        timerhandler.removeCallbacks(runnable)
        timerText!!.text = dataFormat.format(0)
    }

    companion object {
        //タイマーを止めるのに振り続けなければいけない時間（秒）
        private const val shakeTime = 2

        //センサー関連
        private const val GRAPH_REFRESH_PERIOD_MS: Long = 20
        private const val ALPHA = 0.75f
        private val TAG = AlarmNortificationActivity::class.java.simpleName
    }
}