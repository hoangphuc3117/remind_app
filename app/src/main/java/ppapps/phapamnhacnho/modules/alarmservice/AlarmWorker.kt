package ppapps.phapamnhacnho.modules.alarmservice

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ppapps.phapamnhacnho.constant.AlarmConstant

class AlarmWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val alarmCode = inputData.getLong(ppapps.phapamnhacnho.constant.AlarmConstant.KEY_ALARM_CODE, -1)
        if (alarmCode == -1L) return Result.failure()

        // Wake up device
        ppapps.phapamnhacnho.basemodules.util.StaticWakeLock.lockOn(applicationContext)

        // Fetch alarm from database (blocking for simplicity)
        val alarm = try {
            ppapps.phapamnhacnho.basemodules.database.DatabaseFactory.getAlarm(alarmCode)
                .toBlocking()
                .firstOrDefault(null)
        } catch (e: Exception) {
            null
        }
        if (alarm == null) return Result.failure()

        // Show notification
        showAlarmNotification(alarm)

        // Send broadcast to update UI (show alarm popup)
        val intent = android.content.Intent()
        intent.action = ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity.BROADCAST_STRING_SHOW_ALARM_POPUP
        intent.putExtra(ppapps.phapamnhacnho.constant.AlarmConstant.KEY_ALARM, com.google.gson.Gson().toJson(alarm))
        applicationContext.sendBroadcast(intent)

        return Result.success()
    }

    private fun showAlarmNotification(alarm: ppapps.phapamnhacnho.model.AlarmModel) {
        val channelId = ppapps.phapamnhacnho.constant.AlarmConstant.ALARM_NOTIFICATION_CHANNEL_ID
        val notificationManager = applicationContext.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "ALARM NOTIFICATIONS",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "This is channel for alarm"
            notificationManager.createNotificationChannel(channel)
        }
        val builder = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(ppapps.phapamnhacnho.R.mipmap.ic_app)
            .setContentTitle(alarm.name)
            .setContentText(ppapps.phapamnhacnho.basemodules.util.TimeUtil.getDateTimeFromTimeStamp(alarm.time))
            .setOngoing(true)
            .setAutoCancel(false)
        notificationManager.notify(ppapps.phapamnhacnho.constant.AlarmConstant.ALARM_NOTIFICATION_ID, builder.build())
    }
}
