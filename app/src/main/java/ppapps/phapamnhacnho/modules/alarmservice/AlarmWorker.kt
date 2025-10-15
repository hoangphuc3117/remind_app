package ppapps.phapamnhacnho.modules.alarmservice

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import ppapps.phapamnhacnho.constant.AlarmConstant
import ppapps.phapamnhacnho.modules.mediaplayer.MyPlayer

class AlarmWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    
    private var myPlayer: MyPlayer? = null
    
    override fun doWork(): Result {
        Log.d("AlarmWorker", "doWork started")
        val alarmCode = inputData.getLong(AlarmConstant.KEY_ALARM_CODE, -1)
        Log.d("AlarmWorker", "Processing alarm: code=$alarmCode")
        
        if (alarmCode == -1L) {
            Log.e("AlarmWorker", "Invalid alarm code")
            return Result.failure()
        }

        // Wake up device
        ppapps.phapamnhacnho.basemodules.util.StaticWakeLock.lockOn(applicationContext)

        // Fetch alarm from database
        val alarm = try {
            ppapps.phapamnhacnho.basemodules.database.DatabaseFactory.getAlarm(alarmCode)
                .toBlocking()
                .firstOrDefault(null)
        } catch (e: Exception) {
            Log.e("AlarmWorker", "Failed to fetch alarm from database: ${e.message}", e)
            null
        }
        
        if (alarm == null) {
            Log.e("AlarmWorker", "Alarm not found in database: code=$alarmCode")
            return Result.failure()
        }
        
        Log.d("AlarmWorker", "Alarm found: name=${alarm.name}, time=${alarm.time}")

        // Initialize media player for alarm sound
        initializeMediaPlayer(alarm)

        // Show notification
        showAlarmNotification(alarm)

        // Launch AlarmActivity to show alarm popup (works even when app is in background)
        val intent = Intent(applicationContext, ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity::class.java)
        intent.action = ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity.BROADCAST_STRING_SHOW_ALARM_POPUP
        intent.putExtra(AlarmConstant.KEY_ALARM, com.google.gson.Gson().toJson(alarm))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        applicationContext.startActivity(intent)
        Log.d("AlarmWorker", "AlarmActivity launched to show alarm popup")

        // Schedule next alarm if needed (for recurring alarms)
        scheduleNextAlarmIfNeeded(alarm)

        return Result.success()
    }

    private fun initializeMediaPlayer(alarm: ppapps.phapamnhacnho.model.AlarmModel) {
        try {
            Log.d("AlarmWorker", "Initializing media player: fileType=${alarm.fileType}, uri=${alarm.uriFileFolder}")
            myPlayer = MyPlayer(applicationContext)
            myPlayer?.setPlayType(alarm.fileType)
            myPlayer?.setPlayFile(alarm.uriFileFolder)
            myPlayer?.initialMediaPlayer(alarm.fileIndex, alarm.playingPosition)
            Log.d("AlarmWorker", "Media player initialized successfully")
        } catch (e: Exception) {
            // If media player fails, continue with notification only
            Log.e("AlarmWorker", "Failed to initialize media player: ${e.message}", e)
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
            
            // Check if we can schedule exact alarms on Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Permission not granted, cannot schedule next alarm
                    return
                }
            }
            
            val alarmIntent = Intent(applicationContext, ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver::class.java)
            alarmIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarm.code)
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                alarm.code.toInt(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    nextTime,
                    pendingIntent
                )
            } catch (e: SecurityException) {
                // Permission was revoked, cannot schedule alarm
            }
        }
    }

    private fun showAlarmNotification(alarm: ppapps.phapamnhacnho.model.AlarmModel) {
        Log.d("AlarmWorker", "Creating notification for alarm: ${alarm.name}")
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
            channel.enableLights(true)
            channel.setSound(null, null) // We'll handle sound through MediaPlayer
            channel.setShowBadge(true)
            channel.lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        // Create full-screen intent to show alarm popup immediately (even on lock screen)
        val fullScreenIntent = Intent(applicationContext, ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity::class.java)
        fullScreenIntent.action = ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity.BROADCAST_STRING_SHOW_ALARM_POPUP
        fullScreenIntent.putExtra(AlarmConstant.KEY_ALARM, com.google.gson.Gson().toJson(alarm))
        fullScreenIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            alarm.code.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create content intent for when user taps notification
        val contentIntent = Intent(applicationContext, ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity::class.java)
        contentIntent.action = ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity.BROADCAST_STRING_SHOW_ALARM_POPUP
        contentIntent.putExtra(AlarmConstant.KEY_ALARM, com.google.gson.Gson().toJson(alarm))
        contentIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            (alarm.code.toInt() + 1000),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create stop action for notification
        val stopIntent = Intent(applicationContext, AlarmStopReceiver::class.java)
        stopIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarm.code)
        val stopPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            (alarm.code.toInt() + 2000),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = androidx.core.app.NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(ppapps.phapamnhacnho.R.mipmap.ic_app)
            .setContentTitle(alarm.name ?: "Alarm")
            .setContentText("${ppapps.phapamnhacnho.basemodules.util.TimeUtil.getDateTimeFromTimeStamp(alarm.time)}")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                .bigText("${alarm.name ?: "Alarm"}\n${ppapps.phapamnhacnho.basemodules.util.TimeUtil.getDateTimeFromTimeStamp(alarm.time)}"))
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentPendingIntent)
            .addAction(
                ppapps.phapamnhacnho.R.drawable.ic_cancel_100,
                "Dismiss",
                stopPendingIntent
            )
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setVibrate(longArrayOf(0, 1000, 500, 1000)) // Vibration pattern
            .setLights(android.graphics.Color.RED, 3000, 3000) // LED notification
            .setFullScreenIntent(fullScreenPendingIntent, true) // Show full screen on lock screen

        notificationManager.notify(AlarmConstant.ALARM_NOTIFICATION_ID, builder.build())
        Log.d("AlarmWorker", "Notification displayed successfully with full-screen intent")
    }
}
