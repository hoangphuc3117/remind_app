package ppapps.phapamnhacnho.modules.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ppapps.phapamnhacnho.basemodules.database.DatabaseFactory
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver
import android.app.AlarmManager
import android.app.PendingIntent
import ppapps.phapamnhacnho.constant.AlarmConstant

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Fetch all alarms from DB and re-register them
            val alarms = DatabaseFactory.getAlarms().toBlocking().firstOrDefault(null)
            alarms?.let {
                for (alarm in it) {
                    if (alarm.status == AlarmModel.STATUS_IN_PROGRESS) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val alarmIntent = Intent(context, AlarmReceiver::class.java)
                        alarmIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarm.code)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            alarm.code.toInt(),
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarm.time,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }
}
