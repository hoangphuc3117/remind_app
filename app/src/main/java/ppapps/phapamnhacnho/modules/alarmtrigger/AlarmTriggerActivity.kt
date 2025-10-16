package ppapps.phapamnhacnho.modules.alarmtrigger

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.google.gson.Gson
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.basemodules.baseviews.BaseActivity
import ppapps.phapamnhacnho.basemodules.util.TimeUtil
import ppapps.phapamnhacnho.constant.AlarmConstant
import ppapps.phapamnhacnho.databinding.ActivityAlarmTriggerBinding
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.modules.alarmlist.AlarmActivity

class AlarmTriggerActivity : BaseActivity() {

    private lateinit var binding: ActivityAlarmTriggerBinding
    private var alarm: AlarmModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show on lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        binding = ActivityAlarmTriggerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get alarm data from intent
        val alarmJson = intent.getStringExtra(AlarmConstant.KEY_ALARM)
        if (alarmJson != null) {
            alarm = Gson().fromJson(alarmJson, AlarmModel::class.java)
            displayAlarmInfo()
        } else {
            finish()
            return
        }

        setupClickListeners()
    }

    private fun displayAlarmInfo() {
        alarm?.let { alarmModel ->
            // Set alarm name
            binding.alarmTriggerName.text = alarmModel.name

            // Set time
            val time = TimeUtil.getTimeFromTimeStamp(alarmModel.time)
            val date = TimeUtil.getDateFromTimeStamp(alarmModel.time)
            binding.alarmTriggerTime.text = "$time • $date"

            // Set loop info
            binding.alarmTriggerLoopInfo.text = getString(
                R.string.description,
                "${alarmModel.countLoopTimes} / ${alarmModel.loopTime}"
            )

            // File name and progress views are now hidden in layout
            // No need to set them
        }
    }

    private fun setupClickListeners() {
        // Dismiss button - stop alarm completely
        binding.alarmTriggerBtnDismiss.setOnClickListener {
            dismissAlarm()
        }

        // Hide button - minimize to notification
        binding.alarmTriggerBtnHide.setOnClickListener {
            hideAlarm()
        }
    }

    private fun dismissAlarm() {
        // Cancel the notification
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(AlarmConstant.ALARM_NOTIFICATION_ID)
        
        // Cancel any ongoing WorkManager tasks for this alarm
        alarm?.let {
            androidx.work.WorkManager.getInstance(this).cancelAllWorkByTag("alarm_${it.code}")
        }
        
        // Send broadcast to close dialog in AlarmActivity
        val intent = Intent(AlarmActivity.BROADCAST_STRING_CLOSE_DIALOG)
        sendBroadcast(intent)
        
        // Release wake lock
        try {
            ppapps.phapamnhacnho.basemodules.util.StaticWakeLock.lockOff()
        } catch (e: Exception) {
            // Ignore if wake lock wasn't held
        }
        
        finish()
    }

    private fun hideAlarm() {
        // Just minimize the activity, service continues in background
        moveTaskToBack(true)
    }

    companion object {
        const val TAG = "AlarmTriggerActivity"
    }
}
