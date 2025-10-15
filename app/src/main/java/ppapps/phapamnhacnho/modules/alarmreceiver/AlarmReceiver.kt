package ppapps.phapamnhacnho.modules.alarmreceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import ppapps.phapamnhacnho.modules.alarmservice.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("AlarmReceiver", "onReceive called")
        if (context == null || intent == null) {
            Log.e("AlarmReceiver", "Context or Intent is null")
            return
        }

        // Extract alarm code or other data from intent
        val alarmCode = intent.getLongExtra(ppapps.phapamnhacnho.constant.AlarmConstant.KEY_ALARM_CODE, -1)
        Log.d("AlarmReceiver", "Alarm triggered: code=$alarmCode")

        if (alarmCode == -1L) {
            Log.e("AlarmReceiver", "Invalid alarm code")
            return
        }

        // Schedule work with WorkManager with a tag for easier cancellation
        val workData = androidx.work.Data.Builder()
            .putLong(ppapps.phapamnhacnho.constant.AlarmConstant.KEY_ALARM_CODE, alarmCode)
            .build()
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<ppapps.phapamnhacnho.modules.alarmservice.AlarmWorker>()
            .setInputData(workData)
            .addTag("alarm_work")
            .addTag("alarm_$alarmCode")
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(workRequest)
        Log.d("AlarmReceiver", "WorkManager enqueued for alarm: $alarmCode")

        resultCode = Activity.RESULT_OK
    }
}