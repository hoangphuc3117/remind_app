package ppapps.phapamnhacnho.modules.alarmservice

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import androidx.work.Worker
import androidx.work.WorkerParameters
import ppapps.phapamnhacnho.constant.AlarmConstant
import ppapps.phapamnhacnho.modules.mediaplayer.MyPlayer

class AlarmWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    
    private var myPlayer: MyPlayer? = null
    
    override fun doWork(): Result {
        val alarmCode = inputData.getLong(AlarmConstant.KEY_ALARM_CODE, -1)
        if (alarmCode == -1L) return Result.failure()

        // Wake up device
        ppapps.phapamnhacnho.basemodules.util.StaticWakeLock.lockOn(applicationContext)

        // Fetch alarm from database
        val alarm = try {
            ppapps.phapamnhacnho.basemodules.database.DatabaseFactory.getAlarm(alarmCode)
                .toBlocking()
                .firstOrDefault(null)
        } catch (e: Exception) {
            null
        }
        if (alarm == null) return Result.failure()

        // Initialize media player for alarm sound
        initializeMediaPlayer(alarm)

        // Show notification
        showAlarmNotification(alarm)

        // Send broadcast to update UI (show alarm popup)
        val intent = Intent()
        intent.action = ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity.BROADCAST_STRING_SHOW_ALARM_POPUP
        intent.putExtra(AlarmConstant.KEY_ALARM, com.google.gson.Gson().toJson(alarm))
        applicationContext.sendBroadcast(intent)

        // Schedule next alarm if needed (for recurring alarms)
        scheduleNextAlarmIfNeeded(alarm)

        return Result.success()
    }

    private fun initializeMediaPlayer(alarm: ppapps.phapamnhacnho.model.AlarmModel) {
        try {
            myPlayer = MyPlayer(applicationContext)
            myPlayer?.setPlayType(alarm.fileType)
            myPlayer?.setPlayFile(alarm.uriFileFolder)
            myPlayer?.initialMediaPlayer(alarm.fileIndex, alarm.playingPosition)
        } catch (e: Exception) {
            // If media player fails, continue with notification only
            e.printStackTrace()
        }
    }

    private fun scheduleNextAlarmIfNeeded(alarm: ppapps.phapamnhacnho.model.AlarmModel) {
        if (alarm.loopType != ppapps.phapamnhacnho.modules.addeditalarm.adapter.LoopTypeAdapter.NO_LOOP) {
            // Calculate next alarm time based on loop type
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = alarm.time
            
            when (alarm.loopType) {
                ppapps.phapamnhacnho.modules.addeditalarm.adapter.LoopTypeAdapter.LOOP_DAY -> {
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
                ppapps.phapamnhacnho.modules.addeditalarm.adapter.LoopTypeAdapter.LOOP_WEEK -> {
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 7)
                }
                ppapps.phapamnhacnho.modules.addeditalarm.adapter.LoopTypeAdapter.LOOP_MONTH -> {
                    calendar.add(java.util.Calendar.MONTH, 1)
                }
            }
            
            val nextTime = calendar.timeInMillis
            alarm.nextTime = nextTime
            
            // Update alarm in database
            ppapps.phapamnhacnho.basemodules.database.DatabaseFactory.updateAlarm(alarm)
                .toBlocking()
                .firstOrDefault(false)
                
            // Schedule next alarm with AlarmManager
            val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val alarmIntent = Intent(applicationContext, ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver::class.java)
            alarmIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarm.code)
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                alarm.code.toInt(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                nextTime,
                pendingIntent
            )
        }
    }

    private fun showAlarmNotification(alarm: ppapps.phapamnhacnho.model.AlarmModel) {
        val channelId = AlarmConstant.ALARM_NOTIFICATION_CHANNEL_ID
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        // Create notification channel for Android 8.0+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "ALARM NOTIFICATIONS",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "This is channel for alarm"
            channel.enableVibration(true)
            channel.setSound(null, null) // We'll handle sound through MediaPlayer
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open AlarmActivity when notification is tapped
        val contentIntent = Intent(applicationContext, ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity::class.java)
        contentIntent.putExtra(AlarmConstant.KEY_ALARM, com.google.gson.Gson().toJson(alarm))
        contentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarm.code.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create stop action for notification
        val stopIntent = Intent(applicationContext, AlarmStopReceiver::class.java)
        stopIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarm.code)
        val stopPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            alarm.code.toInt(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(ppapps.phapamnhacnho.R.mipmap.ic_app)
            .setContentTitle(alarm.name ?: "Alarm")
            .setContentText(ppapps.phapamnhacnho.basemodules.util.TimeUtil.getDateTimeFromTimeStamp(alarm.time))
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentPendingIntent)
            .addAction(
                ppapps.phapamnhacnho.R.drawable.ic_cancel_100,
                "Dismiss",
                stopPendingIntent
            )
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(contentPendingIntent, true)

        notificationManager.notify(AlarmConstant.ALARM_NOTIFICATION_ID, builder.build())
    }
}
