package ppapps.phapamnhacnho.modules.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ppapps.phapamnhacnho.basemodules.database.DatabaseFactory
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver
import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Build
import ppapps.phapamnhacnho.constant.AlarmConstant

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Check if we can schedule exact alarms on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Permission not granted, cannot schedule exact alarms
                    return
                }
            }
            
            // Fetch all alarms from DB and re-register them
            val alarms = DatabaseFactory.getAlarms().toBlocking().firstOrDefault(null)
            alarms?.let {
                for (alarm in it) {
                    if (alarm.status == AlarmModel.STATUS_IN_PROGRESS) {
                        val alarmIntent = Intent(context, AlarmReceiver::class.java)
                        alarmIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarm.code)
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            alarm.code.toInt(),
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                if (alarm.nextTime > 0) alarm.nextTime else alarm.time,
                                pendingIntent
                            )
                        } catch (e: SecurityException) {
                            // Permission was revoked, skip this alarm
                        }
                    }
                }
            }
        }
    }
}
