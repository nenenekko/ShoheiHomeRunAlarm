package jp.ac.titech.itpro.sdl.shoheihomerunalarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

interface CustomAdapterListener {
    fun clicked(alarm: Alarm,view: View)
}

class CustomAdapter(context: Context, var mAlarmList: List<Alarm>,  val listener: CustomAdapterListener) : ArrayAdapter<Alarm>(context, 0, mAlarmList) {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Alarmの取得
        val alarm = mAlarmList[position]

        // レイアウトの設定
        var view = convertView
        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.alarm_item, parent, false)
        }

        // 各Viewの設定
        val imageView = view?.findViewById<ImageView>(R.id.image)
        imageView?.setImageResource(alarm.imageId)

        val seted_time = view?.findViewById<TextView>(R.id.seted_time)
        var hour_text = alarm.hour.toString()
        var minute_text = alarm.minute.toString()
        if(minute_text.length == 1) minute_text = "0" + minute_text
        seted_time?.text = hour_text + ":" + minute_text

        val button = view?.findViewById<Button>(R.id.button)
        button?.setOnClickListener {
            listener.clicked(alarm,it)
        }

        return view!!
    }

    // リスト更新用の関数を実装
    fun updateAlarmList(alarmList: List<Alarm>) {
        mAlarmList = alarmList
        // 再描画
        notifyDataSetChanged()
    }
}