package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), CustomAdapterListener, TimePickerDialog.OnTimeSetListener {

    lateinit var mCustomAdapter: CustomAdapter
    lateinit var mAlarmList: ArrayList<Alarm>
    lateinit var context: Context
    val SETTINGS_PLAYER_JSON = "alarm_test"
    var sAlarmList: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = applicationContext

        sAlarmList = getStringArrayPref(context, SETTINGS_PLAYER_JSON)
        if (sAlarmList != null) {
            mAlarmList = arrayListOf()
            val listView = findViewById<ListView>(R.id.list_view)
            for (value in sAlarmList!!) {
                val hour_mintue = value.split(":".toRegex()).toTypedArray()
                mAlarmList.add(
                    Alarm(
                        hour_mintue[0].toInt(),
                        hour_mintue[1].toInt(),
                        R.drawable.dog
                    )
                )
                Log.d(TAG, "Get json : $value")
            }
            // CustomAdapterの生成と設定
            mCustomAdapter = CustomAdapter(this, mAlarmList, this)
            listView?.adapter = mCustomAdapter
        }else{
            Log.d(TAG, "アラームデータがありません")
        }

        val str = "Alarm Start"
        button1.setText(str)

        button1.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.SECOND, 5)
            val intent = Intent(
                context,
                AlarmBroadcastReceiver::class.java
            )
            val pending = PendingIntent.getBroadcast(
                context, 0, intent, 0
            )
            // アラームをセットする
            val alarm_manager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (alarm_manager != null) {
                alarm_manager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    SystemClock.elapsedRealtime() + 15 * 1000,
                    pending
                )
                Toast.makeText(
                    context,
                    "Set Alarm ", Toast.LENGTH_SHORT
                ).show()
            }
        }

        button2.setOnClickListener{
            Toast.makeText(
                context, "Bottun2 ", Toast.LENGTH_SHORT
            ).show()
            showTimePickerDialog(it)
        }
    }

    // 追加ボタンがタップされたら呼ばれる
    override fun clicked(alarm: Alarm) {
        mAlarmList.add(alarm)

        // CustomAdapterに実装したリスト更新用の関数を呼ぶ
        mCustomAdapter.updateAnimalList(mAlarmList)
    }

    private fun setStringArrayPref(context: Context, key: String, values: ArrayList<String>?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        val a = JSONArray()
        for (i in 0 until values!!.size) {
            a.put(values[i])
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString())
        } else {
            editor.putString(key, null)
        }
        editor.apply()
    }

    private fun getStringArrayPref(context: Context, key: String): ArrayList<String>? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val json = prefs.getString(key, null)
        val urls = ArrayList<String>()
        if (json != null) {
            try {
                val a = JSONArray(json)
                for (i in 0 until a.length()) {
                    val url: String = a.optString(i)
                    urls.add(url)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return urls
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val value = String.format(Locale.US, "%d:%d", hourOfDay, minute)
        sAlarmList?.add(value)
        setStringArrayPref(context, SETTINGS_PLAYER_JSON, sAlarmList)
        Log.d(TAG, "Put json")

        // use the plug in of Kotlin Android Extensions
        val hour_mintue = value.split(":".toRegex()).toTypedArray()
        mAlarmList.add(Alarm(hour_mintue[0].toInt(),hour_mintue[1].toInt(),R.drawable.dog))
        // CustomAdapterに実装したリスト更新用の関数を呼ぶ
        mCustomAdapter.updateAnimalList(mAlarmList)
    }

    // called by Buttton tapping
    fun showTimePickerDialog(v: View) {
        val newFragment = TimePick()
        newFragment.show(supportFragmentManager, "timePicker")
    }
}