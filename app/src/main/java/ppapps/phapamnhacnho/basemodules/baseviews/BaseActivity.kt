package ppapps.phapamnhacnho.basemodules.baseviews

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.databinding.ActAlarmBinding
import ppapps.phapamnhacnho.sharedmodule.customviews.LoadingFragment

abstract class BaseActivity : AppCompatActivity() {
    private val TAG = BaseActivity::class.java.simpleName

    companion object {
        const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.push_left_in, R.anim.push_up_out)
        }
        // For Android 14+ (API 34+), consider using the new predictive back gesture animations
        // or WindowManager.LayoutParams.FLAG_ACTIVITY_NEW_TASK with proper transition animations
    }

    override fun finish() {
        super.finish()
    }

    fun getImageIdByName(imageName: String?): Int {
        return resources.getIdentifier(imageName, "drawable", this.packageName)
    }

    fun isDeviceOnline(): Boolean {
        val conMgr = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = conMgr.activeNetwork
            val networkCapabilities = conMgr.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = conMgr.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetworkInfo?.isConnected == true
        }
    }

    fun showDialogFragmentWithAllowingStateLoss(dialogFragment: DialogFragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.add(dialogFragment, "").addToBackStack(null).commitAllowingStateLoss()
    }

    fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                true
            } else {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            true
        }
    }

    // For show loading dialog
    private var loadingDialog: LoadingFragment? = null
    private var loadingDialogShowing: Boolean = false

    fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = LoadingFragment.newInstance()
        }
        try {
            if (!loadingDialogShowing) {
                loadingDialog?.setCancelable(true)
                loadingDialog?.show(supportFragmentManager, this.javaClass.simpleName)
                loadingDialogShowing = true
            }
        } catch (ex: Exception) {
        }
    }

    fun hideLoadingDialog() {
        try {
            if (loadingDialog != null) {
                loadingDialog?.dismissAllowingStateLoss()
                loadingDialogShowing = false
            }
        } catch (ex: Exception) {
        }
    }
}