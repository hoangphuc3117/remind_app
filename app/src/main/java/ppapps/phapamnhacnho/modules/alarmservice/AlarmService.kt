package ppapps.phapamnhacnho.modules.alarmservice

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.basemodules.database.DatabaseFactory
import ppapps.phapamnhacnho.basemodules.util.StaticWakeLock
import ppapps.phapamnhacnho.basemodules.util.TimeUtil
import ppapps.phapamnhacnho.constant.AlarmConstant
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.modules.addeditalarm.adapter.LoopTypeAdapter
import ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity
import ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver
import ppapps.phapamnhacnho.modules.mediaplayer.MyPlayer
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

class AlarmService : Service() {
    companion object {
        val ACTION_STOP = "action_stop"
        val ACTION_START = "action_start"
    }

    private val TAG = AlarmService::class.java.simpleName

    private var myPlayer: MyPlayer? = null

    private var alarm: AlarmModel? = null
    private var mCountDownTimer: CountDownTimer? = null
    private var mCountTimes = 1

    override fun onCreate() {
        super.onCreate()

        initListener()
    }

    private fun initListener() {

    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.action != null) {
            val action = intent.action
            if (ACTION_STOP == action) {
                cancelNotification()
                updateAlarm()
                finishAlarmService()
            }
        }
        if (intent != null) {
            val alarmCode = intent.getLongExtra(AlarmConstant.KEY_ALARM_CODE, -1)
            if (alarmCode != -1L) {
                getAlarm(alarmCode)
            }
        }
    }

    private fun finishAlarmService() {
        if (mCountDownTimer != null) {
            mCountDownTimer!!.cancel()
        }
        if (myPlayer != null) {
            myPlayer!!.release()
        }
        //Refresh
        refreshAlarmsInAlarmActivity()
        //Close dialog
        closeDialogInAlarmActivity()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }


    private fun isScreenOn(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            pm.isInteractive
        } else {
            return false
        }
    }

    fun getAlarm(alarmCode: Long) {
        DatabaseFactory.getAlarm(alarmCode)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<AlarmModel?>() {
                override fun onCompleted() {}
                override fun onError(e: Throwable) {}
                override fun onNext(alarmModel: AlarmModel?) {
                    startAlarm(alarmModel)
                }
            })
    }

    private fun updateAlarm(alarm: AlarmModel?) {
        DatabaseFactory.updateAlarm(alarm)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<Boolean>() {
                override fun onCompleted() {}
                override fun onError(e: Throwable) {}
                override fun onNext(update: Boolean) {
                }
            })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initMediaSessions(alarm: AlarmModel?) {
        if (alarm == null) {
            return
        }
        if (myPlayer != null) {
            myPlayer!!.release()
        }
        generateNotification()
        myPlayer = MyPlayer(this)
        myPlayer!!.setPlayType(this.alarm!!.fileType)
        myPlayer!!.setPlayFile(this.alarm!!.uriFileFolder)
        myPlayer!!.initialMediaPlayer(this.alarm!!.fileIndex, this.alarm!!.playingPosition)
        createCountDownTimer(this.alarm!!.timeAlarm)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun generateNotification() {
        val alamNotificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            NotificationCompat.Builder(
                this, AlarmConstant.ALARM_NOTIFICATION_CHANNEL_ID
            )
        } else {
            // For pre-O versions, use default channel
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(this)
        }

        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_app)
        val intent = Intent(this, AlarmActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(AlarmConstant.KEY_ALARM, Gson().toJson(alarm))
        val contentIntent =
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val mStopServiceIntent = Intent(this, AlarmService::class.java)
        mStopServiceIntent.action = ACTION_STOP
        val contentIntent1 = PendingIntent.getService(this, 0, mStopServiceIntent, 0)
        alamNotificationBuilder.setSmallIcon(R.mipmap.ic_app)
            .setLargeIcon(largeIcon)
            .setContentText(TimeUtil.getDateTimeFromTimeStamp(alarm?.time ?: 0))
            .addAction(
                R.drawable.ic_cancel_black_100,
                getString(R.string.dialog_dismiss),
                contentIntent1
            )
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
        val style: NotificationCompat.BigTextStyle =
            NotificationCompat.BigTextStyle(alamNotificationBuilder)
        style.setBigContentTitle(alarm?.name)
            .bigText(TimeUtil.getDateTimeFromTimeStamp(alarm?.time ?: 0))
        alamNotificationBuilder.setStyle(style)

        val notificationManager1 = NotificationManagerCompat.from(this)
        val notification = alamNotificationBuilder.build()
        
        // Start as foreground service for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(AlarmConstant.ALARM_NOTIFICATION_ID, notification)
        } else {
            // For older versions, just notify
            notificationManager1.notify(AlarmConstant.ALARM_NOTIFICATION_ID, notification)
        }
    }

    private fun cancelNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmConstant.ALARM_NOTIFICATION_ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val mChannel = NotificationChannel(
            AlarmConstant.ALARM_NOTIFICATION_CHANNEL_ID,
            "ALARM NOTIFICATIONS",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        mChannel.description = "This is channel for alarm"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    fun createCountDownTimer(seconds: Long) {
        mCountDownTimer = object : CountDownTimer(seconds * 1000, 100) {
            override fun onTick(l: Long) {}
            override fun onFinish() {
                mCountTimes++
                if (mCountTimes >= alarm?.loopTime ?: 0) {
                    updateAlarm()
                    finishAlarmService()
                } else {
                    resetMediaPlayer()
                    updateCountTimesInAlarmActivity()
                    createCountDownTimer(alarm?.timeAlarm ?: 0)
                }
            }
        }.start()
    }

    private fun updateCountTimesInAlarmActivity() {
        val broadcastIntent = Intent()
        broadcastIntent.action = AlarmActivity.BROADCAST_STRING_UPDATE_COUNT_TIMES
        broadcastIntent.putExtra(AlarmConstant.KEY_ALARM_COUNT_TIMES, mCountTimes)
        sendBroadcast(broadcastIntent)
    }

    private fun closeDialogInAlarmActivity() {
        val broadcastIntent = Intent()
        broadcastIntent.action = AlarmActivity.BROADCAST_STRING_CLOSE_DIALOG
        broadcastIntent.putExtra(AlarmConstant.KEY_ALARM_COUNT_TIMES, mCountTimes)
        sendBroadcast(broadcastIntent)
    }

    private fun showAlarmPopupActivity() {
        val broadcastIntent = Intent()
        broadcastIntent.action = AlarmActivity.BROADCAST_STRING_SHOW_ALARM_POPUP
        broadcastIntent.putExtra(AlarmConstant.KEY_ALARM, Gson().toJson(this.alarm))
        sendBroadcast(broadcastIntent)
    }

    private fun refreshAlarmsInAlarmActivity() {
        val broadcastIntent = Intent()
        broadcastIntent.action = AlarmActivity.BROADCAST_STRING_REFRESH_ALARMS
        sendBroadcast(broadcastIntent)
    }

    private fun updateAlarm() {
        if (alarm == null) {
            return
        }
        if (alarm?.loopType == LoopTypeAdapter.NO_LOOP) {
            alarm?.status = AlarmModel.STATUS_DONE
        }
        updateCurrentSongAndPlayingPosition()
        alarm?.countLoopTimes = 1
        alarm?.time = alarm?.nextTime ?: 0
        updateAlarm(alarm)
    }


    private fun updateCurrentSongAndPlayingPosition() {
        if (alarm != null) {
            updatePlayingPosition()
            alarm?.fileIndex = getNewFileIndex()
            alarm?.playingPosition = getPlayingPosition()
        }
    }

    fun resetMediaPlayer() {
        myPlayer!!.resetPlayer()
    }

    fun getFileIndex(): Int {
        return myPlayer!!.fileIndex
    }

    fun getNewFileIndex(): Int {
        return myPlayer!!.newFileIndex
    }

    fun getPlayingPosition(): Int {
        return myPlayer!!.playingPosition
    }

    fun updatePlayingPosition() {
        myPlayer!!.updatePlayingPosition()
    }

    fun getFileName(): String? {
        return myPlayer!!.fileName
    }

    override fun onDestroy() {
        if (myPlayer != null) {
            myPlayer!!.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun startAlarm(alarm: AlarmModel?) {
        this.alarm = alarm
        if (this.alarm == null) return
        //Register next alarm for alarm manager
        if (this.alarm?.loopType != LoopTypeAdapter.NO_LOOP) setNextAlarm(
            this.alarm?.code?.toInt() ?: 0,
            this.alarm?.loopType ?: 0,
            this.alarm?.time ?: 0
        ) else this.alarm?.nextTime = this.alarm?.time ?: 0
        StaticWakeLock.lockOn(this)
        initMediaSessions(alarm)
        if (isScreenOn()) {
            showAlarmPopupActivity()
        }
    }

    fun setNextAlarm(id: Int, loopType: Int, currentTime: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        if (loopType == LoopTypeAdapter.LOOP_DAY) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        } else if (loopType == LoopTypeAdapter.LOOP_WEEK) {
            calendar.add(Calendar.DAY_OF_MONTH, 7)
        } else if (loopType == LoopTypeAdapter.LOOP_MONTH) {
            calendar.add(Calendar.MONTH, 1)
        }
        //        } else if (loopType == LoopTypeAdapter.TWO_MINUTES) {
//            calendar.add(Calendar.MINUTE, 2);
//        } else if (loopType == LoopTypeAdapter.THREE_MINUTES) {
//            calendar.add(Calendar.MINUTE, 3);
//        } else if (loopType == LoopTypeAdapter.FIVE_MINUTES) {
//            calendar.add(Calendar.MINUTE, 5);
//        } else if (loopType == LoopTypeAdapter.TEN_MINUTES) {
//            calendar.add(Calendar.MINUTE, 10);
//        } else if (loopType == LoopTypeAdapter.FIFTEEN_MINUTES) {
//            calendar.add(Calendar.MINUTE, 15);
//        } else if (loopType == LoopTypeAdapter.TWENTY_MINUTES) {
//            calendar.add(Calendar.MINUTE, 20);
//        } else if (loopType == LoopTypeAdapter.TWENTY_FIVE_MINUTES) {
//            calendar.add(Calendar.MINUTE, 25);
//        } else if (loopType == LoopTypeAdapter.THIRTY_MINUTES) {
//            calendar.add(Calendar.MINUTE, 30);
//        } else if (loopType == LoopTypeAdapter.SIXTY_MINUTES) {
//            calendar.add(Calendar.MINUTE, 60);
//        }
        val nextTime = calendar.timeInMillis
        alarm?.nextTime = nextTime
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val myIntent = Intent(this, AlarmReceiver::class.java)
        myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, id)
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, id, myIntent, 0)
        alarmManager[AlarmManager.RTC_WAKEUP, nextTime] = pendingIntent
    }

    private fun startActivity(alarm: AlarmModel?) {
        val intent = Intent(this, AlarmActivity::class.java)
        intent.putExtra(AlarmConstant.KEY_ALARM, Gson().toJson(alarm))
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}