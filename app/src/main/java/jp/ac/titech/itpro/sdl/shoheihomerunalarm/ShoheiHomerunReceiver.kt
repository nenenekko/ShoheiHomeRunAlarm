package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class ShoheiHomerunReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // toast で受け取りを確認
        Log.v("nullpo", "HomeRun Check!!!")
        Toast.makeText(context, "Received ", Toast.LENGTH_LONG).show()
    }
}