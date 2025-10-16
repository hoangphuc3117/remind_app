package ppapps.phapamnhacnho.modules.alarmservice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ppapps.phapamnhacnho.modules.mediaplayer.MyPlayer

class MusicStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MusicStopReceiver", "Received broadcast to stop music")
        
        // Stop music through MyPlayer singleton/static method
        try {
            MyPlayer.stopCurrentMusic()
            Log.d("MusicStopReceiver", "Music stopped successfully")
        } catch (e: Exception) {
            Log.e("MusicStopReceiver", "Failed to stop music: ${e.message}", e)
        }
    }
}
