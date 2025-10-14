package ppapps.phapamnhacnho.modules.alarmservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import ppapps.phapamnhacnho.constant.AlarmConstant

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmCode = intent.getLongExtra(AlarmConstant.KEY_ALARM_CODE, -1)
        
        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmConstant.ALARM_NOTIFICATION_ID)
        
        // Cancel any ongoing WorkManager tasks for this alarm
        androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("alarm_$alarmCode")
        
        // Send broadcast to close dialog in AlarmActivity
        val closeDialogIntent = Intent()
        closeDialogIntent.action = ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity.BROADCAST_STRING_CLOSE_DIALOG
        context.sendBroadcast(closeDialogIntent)
        
        // Release wake lock
        try {
            ppapps.phapamnhacnho.basemodules.util.StaticWakeLock.lockOff()
        } catch (e: Exception) {
            // Ignore if wake lock wasn't held
        }
    }
}