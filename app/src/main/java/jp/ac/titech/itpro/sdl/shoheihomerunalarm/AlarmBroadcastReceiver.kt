package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import twitter4j.Query
import twitter4j.QueryResult
import twitter4j.TwitterFactory

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notification = Intent(context, AlarmSwingActivity::class.java)
        //画面起動に必要
        notification.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context!!.startActivity(notification)
    }
}