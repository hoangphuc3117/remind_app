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

    // Deprecated: AlarmService logic has been migrated to WorkManager (AlarmWorker)
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = super.onStartCommand(intent, flags, startId)
    override fun onCreate() { super.onCreate() }
    override fun onDestroy() { super.onDestroy() }
}