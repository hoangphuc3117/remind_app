package ppapps.phapamnhacnho.modules.alarmreceiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import ppapps.phapamnhacnho.modules.alarmservice.AlarmService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val comp = context?.let {
            ComponentName(
                it.packageName,
                AlarmService::class.java.name
            )
        }
        
        // Use modern foreground service approach for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.let { ctx ->
                intent?.setComponent(comp)?.let { serviceIntent ->
                    ContextCompat.startForegroundService(ctx, serviceIntent)
                }
            }
        } else {
            context?.let { ctx ->
                intent?.setComponent(comp)?.let { serviceIntent ->
                    ctx.startService(serviceIntent)
                }
            }
        }
        
        resultCode = Activity.RESULT_OK
    }
}