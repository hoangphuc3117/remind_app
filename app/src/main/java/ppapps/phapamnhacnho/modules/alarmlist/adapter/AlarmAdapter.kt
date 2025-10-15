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
        
        // Set alarm name
        alarmHolder.binding.tvItemName.text = alarm.name
        
        // Set time only (HH:mm format)
        alarmHolder.binding.tvItemTime.text = TimeUtil.getTimeFromTimeStamp(alarm.time)
        
        // Set date separately (dd/MM format)
        alarmHolder.binding.tvItemDate.text = TimeUtil.getDateFromTimeStamp(alarm.time)
        
        // Set alarm number
        alarmHolder.binding.tvItemNumber.text = (position + 1).toString()
        
        // Set status text and visibility
        if (AlarmModel.STATUS_DONE == alarm.status) {
            alarmHolder.binding.tvItemStatus.visibility = View.GONE
            // Use a subtle color tint for completed alarms
            alarmHolder.binding.llContainer.alpha = 0.6f
        } else {
            alarmHolder.binding.tvItemStatus.visibility = View.VISIBLE
            alarmHolder.binding.tvItemStatus.text = "Hoạt động"
            alarmHolder.binding.llContainer.alpha = 1.0f
        }

        // Handle card click - Go directly to detail screen
        alarmHolder.binding.alarmCard.setOnClickListener {
            alarmActivity.showDetailAlarmActivity(alarm)
        }
    }


    override fun getItemCount(): Int {
        return alarmList.size
    }

    fun getAlarmAt(position: Int): AlarmModel {
        return alarmList[position]
    }

    fun removeAlarm(alarmId: Long) {
        alarmList.removeAlarm(alarmId)
        notifyDataSetChanged()
    }

    fun removeAlarmAt(position: Int) {
        if (position >= 0 && position < alarmList.size) {
            alarmList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun restoreAlarm(alarm: AlarmModel, position: Int) {
        alarmList.add(position, alarm)
        notifyItemInserted(position)
    }

    class AlarmHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemAlarmBinding = ItemAlarmBinding.bind(itemView)
    }
}