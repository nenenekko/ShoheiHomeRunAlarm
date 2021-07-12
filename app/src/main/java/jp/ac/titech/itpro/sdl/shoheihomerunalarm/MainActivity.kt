package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.CompoundButton
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
    val SHOHEI_SYSTEM = "shohei_system"
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
                                R.drawable.shohei_hudan
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

        val toggle = findViewById<View>(R.id.toggle_switch) as CompoundButton
        val is_system_on = getPref(context, SHOHEI_SYSTEM)
        if(is_system_on == null || ! string2bool(is_system_on)){
            toggle.isChecked = false
        }else{
            toggle.isChecked = true
        }

        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(
                        context, "ON", Toast.LENGTH_SHORT
                ).show()
                setPref(context,SHOHEI_SYSTEM,"true")
                unsetShoheiSystem(SHOHEI_SYSTEM)
            } else {
                Toast.makeText(
                        context, "OFF", Toast.LENGTH_SHORT
                ).show()
                setPref(context,SHOHEI_SYSTEM,"false")
                setShoheiSystem(SHOHEI_SYSTEM)
            }
        }

        button2.setOnClickListener{
            Toast.makeText(
                    context, "Bottun2 ", Toast.LENGTH_SHORT
            ).show()
            showTimePickerDialog(it)
        }

        button3.setOnClickListener{
            Toast.makeText(
                    context, "予定を全て削除します ", Toast.LENGTH_SHORT
            ).show()
            for(i in 0 until mAlarmList.size){
                unsetAlarm(i)
                Log.d(TAG, i.toString())
            }
            sAlarmList?.clear()
            setStringArrayPref(context, SETTINGS_PLAYER_JSON, sAlarmList)
            mAlarmList.clear()
            mCustomAdapter.updateAlarmList(mAlarmList)
        }

        button4.setOnClickListener{
            //アラームを受け取って起動するActivityを指定、起動
            val notification = Intent(context, WebpageActivity::class.java)
            //画面起動に必要
            notification.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context!!.startActivity(notification)
        }

        button5.setOnClickListener{
            val notification = Intent(context, OhtaniClassifierActivity::class.java)
            //画面起動に必要
            notification.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context!!.startActivity(notification)
        }
    }

    // 追加ボタンがタップされたら呼ばれる
    override fun clicked(alarm: Alarm, view: View) {
        val value = alarm.hour.toString() + ":" + alarm.minute.toString()
        sAlarmList?.remove(value)
        setStringArrayPref(context, SETTINGS_PLAYER_JSON, sAlarmList)
        Log.d(TAG, "Put json")
        mAlarmList.remove(alarm)
        showTimePickerDialog(view)
    }

    fun string2bool(string: String):Boolean{
        if(string == "true") return true
        else                 return false
    }

    private fun setPref(context: Context, key: String, value:String){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getPref(context: Context, key: String): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val value = prefs.getString(key, null)
        return  value
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
        //アラーム時刻を記憶
        val value = String.format(Locale.US, "%d:%d", hourOfDay, minute)
        sAlarmList?.add(value)
        setStringArrayPref(context, SETTINGS_PLAYER_JSON, sAlarmList)
        Log.d(TAG, "Put json")

        //アラーム時刻をリストで表示
        val hour_mintue = value.split(":".toRegex()).toTypedArray()
        mAlarmList.add(Alarm(hour_mintue[0].toInt(), hour_mintue[1].toInt(), R.drawable.shohei_hudan))
        // CustomAdapterに実装したリスト更新用の関数を呼ぶ
        mCustomAdapter.updateAlarmList(mAlarmList)

        for(i in 0 until mAlarmList.size + 1){
            unsetAlarm(i)
            Log.d(TAG, i.toString())
        }
        for(i in 0 until mAlarmList.size + 1){
            if(i != mAlarmList.size) {
                setAlarm(mAlarmList[i].hour, mAlarmList[i].minute, i)
            }else{
                setAlarm(hourOfDay, minute, i)
            }
        }
    }

    // called by Buttton tapping
    fun showTimePickerDialog(v: View) {
        val newFragment = TimePick()
        newFragment.show(supportFragmentManager, "timePicker")
    }

    fun setAlarm(alarmHour: Int, alarmMinute: Int, index: Int){
        val intent = Intent(
                context,
                AlarmBroadcastReceiver::class.java
        )
        intent.setType(index.toString())
        val pending = PendingIntent.getBroadcast(
                context, 0, intent, 0
        )
        //アラーム時間設定
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()

        //設定した時刻をカレンダーに設定
        cal[Calendar.HOUR_OF_DAY] = alarmHour
        cal[Calendar.MINUTE] = alarmMinute
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0

        if (cal.timeInMillis < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        //Toast.makeText(context, java.lang.String.format("%02d時%02d分に起こします", alarmHour, alarmMinute), Toast.LENGTH_LONG).show()

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am[AlarmManager.RTC_WAKEUP, cal.timeInMillis] = pending
        Log.v(TAG, cal.timeInMillis.toString() + "ms")
        Log.v(TAG, "アラームセット完了")
    }

    fun unsetAlarm(index: Int){
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.type = index.toString() // このsetTypeが重要
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }

    fun setShoheiSystem(key: String){
        val intent = Intent(
                context,
                ShoheiHomerunReceiver::class.java
        )
        intent.setType(key)
        val pending = PendingIntent.getBroadcast(
                context, 0, intent, 0
        )
        //アラーム時間設定
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()

        //設定した時刻をカレンダーに設定
        cal[Calendar.HOUR_OF_DAY] = 5
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        cal[Calendar.MILLISECOND] = 0

        if (cal.timeInMillis < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am[AlarmManager.RTC_WAKEUP, cal.timeInMillis] = pending
        Log.v(TAG, cal.timeInMillis.toString() + "ms")
        Log.v(TAG, "アラームセット完了")
    }

    fun unsetShoheiSystem(key: String){
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ShoheiHomerunReceiver::class.java)
        intent.type = key // このsetTypeが重要
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        pendingIntent.cancel()
        alarmManager.cancel(pendingIntent)
    }
}