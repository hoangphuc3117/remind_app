package ppapps.phapamnhacnho.modules.alarmlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.basemodules.util.TimeUtil
import ppapps.phapamnhacnho.databinding.ItemAlarmBinding
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.model.AlarmModelList
import ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity

class AlarmAdapter(val alarmActivity: AlarmActivity) : RecyclerView.Adapter<AlarmAdapter.AlarmHolder>() {

    private val alarmList = AlarmModelList()

    fun setAlarmList(alarmList: AlarmModelList) {
        if (this.alarmList.size > 0) {
            this.alarmList.clear()
        }
        this.alarmList.addAll(alarmList)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, id: Int): AlarmHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_alarm, viewGroup, false)
        return AlarmHolder(itemView)
    }

    override fun onBindViewHolder(alarmHolder: AlarmHolder, position: Int) {
        val alarm: AlarmModel = alarmList[position]
        alarmHolder.binding.tvItemName.text = alarm.name
        alarmHolder.binding.tvItemTime.text = TimeUtil.getDateTimeFromTimeStamp(alarm.time)
        alarmHolder.binding.tvItemNumber.text = (position + 1).toString()
        if (AlarmModel.STATUS_DONE == alarm.status) {
            alarmHolder.binding.llContainer.setBackgroundColor(
                ResourcesCompat.getColor(
                    alarmHolder.binding.tvItemNumber.resources,
                    R.color.color_grey_400,
                    null
                )
            )
        } else {
            alarmHolder.binding.llContainer.setBackgroundColor(
                ResourcesCompat.getColor(
                    alarmHolder.binding.llContainer.resources,
                    R.color.color_white,
                    null
                )
            )
        }

        alarmHolder.binding.llContainer.tag = alarmList[position]
        alarmHolder.binding.llContainer.setOnLongClickListener {
            alarmActivity.showPopupMenu(it)
            true
        }
    }


    override fun getItemCount(): Int {
        return alarmList.size
    }

    fun removeAlarm(alarmId: Long) {
        alarmList.removeAlarm(alarmId)
        notifyDataSetChanged()
    }

    class AlarmHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemAlarmBinding = ItemAlarmBinding.bind(itemView)
    }
}