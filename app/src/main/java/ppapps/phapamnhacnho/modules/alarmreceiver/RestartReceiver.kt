package ppapps.phapamnhacnho.modules.alarmreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        if ("android.intent.action.BOOT_COMPLETED" == intent!!.action) {
//            // It is better to reset alarms using Background IntentService
//            val i = Intent(context, RestartService::class.java)
//            val service = context!!.startService(i)
//            if (null == service) {
//                // something really wrong here
//                Log.e("Restart Receiver", "Could not start service ")
//            } else {
//                Log.e("Restart Receiver", "Successfully started service ")
//            }
//        } else {
//            Log.e("Restart Receiver", "Received unexpected intent $intent")
//        }
    }
}