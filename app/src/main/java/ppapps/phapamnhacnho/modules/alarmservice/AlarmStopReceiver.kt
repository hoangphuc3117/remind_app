package ppapps.phapamnhacnho.modules.alarmservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import ppapps.phapamnhacnho.constant.AlarmConstant

class AlarmStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmCode = intent.getLongExtra(AlarmConstant.KEY_ALARM_CODE, -1)
        android.util.Log.d("AlarmStopReceiver", "onReceive called for alarm: $alarmCode")
        
        // Stop music immediately
        ppapps.phapamnhacnho.modules.mediaplayer.MyPlayer.stopCurrentMusic()
        
        // Cancel the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmConstant.ALARM_NOTIFICATION_ID)
        
        // Cancel any ongoing WorkManager tasks for this alarm
        androidx.work.WorkManager.getInstance(context).cancelAllWorkByTag("alarm_$alarmCode")
        
        // Close AlarmTriggerActivity by starting it with FINISH flag
        // This works even across different task instances
        val closeIntent = Intent(context, ppapps.phapamnhacnho.modules.alarmtrigger.AlarmTriggerActivity::class.java)
        closeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        closeIntent.putExtra("FINISH_ACTIVITY", true)
        try {
            context.startActivity(closeIntent)
        } catch (e: Exception) {
            // Activity might not be running, ignore
            android.util.Log.d("AlarmStopReceiver", "AlarmTriggerActivity not running: ${e.message}")
        }
        
        // Release wake lock
        try {
            ppapps.phapamnhacnho.basemodules.util.StaticWakeLock.lockOff()
        } catch (e: Exception) {
            // Ignore if wake lock wasn't held
        }
    }
}