package ppapps.phapamnhacnho.basemodules.util;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class StaticWakeLock {

    private static PowerManager.WakeLock wl = null;

    /**
     * try to open screen if device is lock
     *
     * @param context app ctx
     */
    public static void lockOn(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //Object flags;
            if (wl == null)
                wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MATH_ALARM");
            if (wl.isHeld())
                wl.release();
            wl.acquire();
        } catch (Exception e) {
            Log.e("error on lock on method", e.getMessage());
        }
    }

    public static void lockOff() {
        try {
            if (wl != null) {
                wl.release();
            }
        } catch (Exception e) {
            Log.e("error on lock on method", e.getMessage());
        }
    }

}
