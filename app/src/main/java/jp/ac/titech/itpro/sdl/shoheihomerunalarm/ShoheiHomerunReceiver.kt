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

class ShoheiHomerunReceiver : BroadcastReceiver() {
    var HomeRunCounter = 30
    var user_list = listOf("30R9gmaMUy3guDJ","vvMLBvv","Ohtani_MLB") //"livedoornews","YahooNewsTopics"
    var homerun_words = listOf("大谷", "号")


    override fun onReceive(context: Context?, intent: Intent?) {
        // toast で受け取りを確認
        Toast.makeText(context, "Received ", Toast.LENGTH_LONG).show()

        var counter = 0
        val job = CoroutineScope(Dispatchers.Default).launch {
            while(true){
                Log.d("status", "繰り返し中")
                val homerun = async(context = Dispatchers.IO) {
                    checkInfluentialUser()
                }.await()
                if(homerun){
                    Log.d("status", "ホームラン！！！！！")
                    //アラームを受け取って起動するActivityを指定、起動
                    val notification = Intent(context, AlarmSwingActivity::class.java)
                    //画面起動に必要
                    notification.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context!!.startActivity(notification)
                    break
                }
                delay(60 * 1000)
                counter++
                if(counter >= 300) break
            }
        }
    }

    suspend fun checkInfluentialUser():Boolean {
        var homerun_count = 0

        val tf = TwitterFactory()
        val twitter = tf.getInstance()
        twitter.oAuth2Token                   //ベアラートークンの取得
        for (i in 0 until user_list.size) {
            var query1 = Query().query("from:" + user_list[i])
            var result: QueryResult = twitter.search(query1)

            for (status in result.tweets) {            //取得したツイート数分ログに出力
                Log.d("status", status.text)
                val regex = Regex(status.text)
                var hit_word_count = 0
                for(word in homerun_words){
                    if(regex.containsMatchIn(word)){
                        hit_word_count++
                    }
                }
                if(hit_word_count == homerun_words.size){
                    homerun_count++
                    break
                }
            }
            Log.d("status", "☆☆☆☆☆☆☆☆☆")
        }
        return homerun_count >= user_list.size / 2
    }
}