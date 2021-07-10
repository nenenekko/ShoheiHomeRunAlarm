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
    fun clicked(alarm: Alarm)
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

        val name = view?.findViewById<TextView>(R.id.name)
        name?.text = alarm.hour.toString() + ":" + alarm.minute.toString()

        val age = view?.findViewById<TextView>(R.id.age)
        age?.text = "${alarm.hour} 才"

        val button = view?.findViewById<Button>(R.id.button)
        button?.setOnClickListener {
            listener.clicked(alarm)
        }

        return view!!
    }

    // リスト更新用の関数を実装
    fun updateAnimalList(alarmList: List<Alarm>) {
        mAlarmList = alarmList
        // 再描画
        notifyDataSetChanged()
    }
}