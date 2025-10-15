package ppapps.phapamnhacnho.modules.alarmlist

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.basemodules.baseviews.BaseActivity
import ppapps.phapamnhacnho.basemodules.util.AppUtil
import ppapps.phapamnhacnho.basemodules.util.TimeUtil
import ppapps.phapamnhacnho.constant.AlarmConstant
import ppapps.phapamnhacnho.databinding.ActAlarmBinding
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.model.AlarmModelList
import ppapps.phapamnhacnho.modules.addeditalarm.AddEditAlarmActivity
import ppapps.phapamnhacnho.modules.alarmlist.adapter.AlarmAdapter
import ppapps.phapamnhacnho.modules.alarmlist.dialog.AlarmDialog
import ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver
import ppapps.phapamnhacnho.modules.alarmservice.AlarmService
import ppapps.phapamnhacnho.sharedmodule.customviews.ConfirmDialog
import ppapps.phapamnhacnho.sharedmodule.customviews.InfoDialog
import java.util.*

class AlarmActivity : BaseActivity() {
    companion object {
        const val BROADCAST_STRING_UPDATE_COUNT_TIMES =
            "ppapps.phapamnhacnho.updatecounttimesdialog"
        const val BROADCAST_STRING_CLOSE_DIALOG = "ppapps.phapamnhacnho.closedialog"
        const val BROADCAST_STRING_REFRESH_ALARMS = "ppapps.phapamnhacnho.refreshalarms"
        const val BROADCAST_STRING_SHOW_ALARM_POPUP = "ppapps.phapamnhacnho.showalarmpopup"
        private const val TAG = "AlarmActivity"
    }
    
    // Notification permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
            // Show explanation to user
            val dialog = InfoDialog()
            dialog.setMessage(getString(R.string.app_name))
            dialog.setDescription("Bạn cần cấp quyền thông báo để nhận nhắc nhở khi app đóng. " +
                "Vui lòng bật trong Settings > Apps > ${getString(R.string.app_name)} > Notifications")
            dialog.show(supportFragmentManager, "notification_permission")
        }
    }

    private lateinit var binding: ActAlarmBinding

    private lateinit var viewModel: AlarmViewModel

    private var isBound = false

    private var connection: ServiceConnection? = null

    private var alarm: AlarmModel? = null

    private var alarmDialog: AlarmDialog? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    private var intentFilter: IntentFilter? = null

    private var firstStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable showing on lock screen for alarm notifications (Android 8.1+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // For older Android versions
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        
        binding = ActAlarmBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        initView()
        initData()
        
        // Handle alarm popup intent if launched from AlarmWorker
        handleAlarmIntent(intent)
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show explanation to user
                    val dialog = InfoDialog()
                    dialog.setMessage(getString(R.string.app_name))
                    dialog.setDescription("App cần quyền thông báo để gửi nhắc nhở khi app đóng. Vui lòng cho phép.")
                    dialog.show(supportFragmentManager, "notification_permission_rationale")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        
        // Also check if notifications are enabled at app level
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled at system level for this app")
        }
    }
    
    private fun handleAlarmIntent(intent: Intent?) {
        if (intent?.action == BROADCAST_STRING_SHOW_ALARM_POPUP) {
            val alarmJson = intent.getStringExtra(AlarmConstant.KEY_ALARM)
            if (alarmJson != null) {
                val alarm = Gson().fromJson(alarmJson, AlarmModel::class.java)
                showAlarm(alarm)
            }
        }
    }

    private fun initView() {
        initToolbar()
        initReceiver()
        initEventListener()
        initListener()
    }

    private fun initEventListener() {
        // Material 3 toolbar - no need for custom icon listeners
        // The toolbar is now a MaterialToolbar from the layout

        binding.fabAddAlarm.setOnClickListener {
            val intent = Intent(this, AddEditAlarmActivity::class.java)
            intent.putExtra(AlarmConstant.KEY_ADD_EDIT_DETAIL_ALARM, AddEditAlarmActivity.ADD_ALARM)
            startActivity(intent)
        }
    }

    private fun initListener() {
        viewModel.liveDataLoading.observe(this, Observer {
            if (it) {
                showLoadingDialog()
            } else {
                hideLoadingDialog()
            }
        })

        viewModel.liveDataAlarms.observe(this, Observer {
            loadAlarmsOnUI(it)
        })
    }

    private fun initData() {
        viewModel.getAlarms()
    }

    private fun initToolbar() {
        // Material 3 toolbar is set in XML with title
        // Setup menu item click listener if needed
        binding.alarmToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // Add menu item handling here if needed
                else -> false
            }
        }
    }

    private fun isServiceRunning(): Boolean {
        // Note: getRunningServices is deprecated but there's no direct replacement
        // for checking if your own service is running. For production apps, consider
        // using a bound service pattern or tracking service state internally.
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("AlarmService" == service.service.className) {
                return true;
            }
        }
        return false;
    }

    private fun setAlarmRecyclerView(alarmList: AlarmModelList) {
        val alarmAdapter = AlarmAdapter(this)
        alarmAdapter.setAlarmList(alarmList)
        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.alarmRlvAlarms.layoutManager = layoutManager
        binding.alarmRlvAlarms.adapter = alarmAdapter
        binding.alarmRlvAlarms.setHasFixedSize(true)
    }

    private fun initReceiver() {
        intentFilter = IntentFilter()
        intentFilter!!.addAction(BROADCAST_STRING_UPDATE_COUNT_TIMES)
        intentFilter!!.addAction(BROADCAST_STRING_CLOSE_DIALOG)
        intentFilter!!.addAction(BROADCAST_STRING_REFRESH_ALARMS)
        intentFilter!!.addAction(BROADCAST_STRING_SHOW_ALARM_POPUP)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (BROADCAST_STRING_UPDATE_COUNT_TIMES == intent.action) {
//                    int countTimes = intent.getIntExtra(AlarmConstant.KEY_ALARM_COUNT_TIMES, -1);
//                    if (mAlarmDialog != null)
//                        mAlarmDialog.updateDescription(getResources().getString(R.string.description, countTimes + ""));
                } else if (BROADCAST_STRING_CLOSE_DIALOG == intent.action) {
                    if (alarmDialog != null) alarmDialog!!.dismiss()
                } else if (BROADCAST_STRING_REFRESH_ALARMS == intent.action) {
                    // Refresh alarm list when broadcast is received
                    viewModel.getAlarms()
                } else if (BROADCAST_STRING_SHOW_ALARM_POPUP == intent.action) {
                    val alarmJson = intent.getStringExtra(AlarmConstant.KEY_ALARM)
                    val alarm = Gson().fromJson(alarmJson, AlarmModel::class.java)
                    showAlarm(alarm)
                }
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    fun showAlarm(alarm: AlarmModel) {
        //hideLoadingIcon()
        this.alarm = alarm
        
        // Check if dialog is already showing, if so dismiss it first
        if (alarmDialog != null && alarmDialog!!.isAdded) {
            try {
                alarmDialog!!.dismissAllowingStateLoss()
            } catch (e: Exception) {
                // Ignore if already dismissed
            }
            alarmDialog = null
        }
        
        // Create new dialog instance
        alarmDialog = AlarmDialog()
        alarmDialog!!.setCallBackListener(object : AlarmDialog.CallBackListener {
            override fun onDismiss() {
                finishAlarm()
            }

            override fun onHide() {
                viewModel.getAlarms()
            }
        })
        alarmDialog!!.setMessage(alarm.name)
        alarmDialog!!.setTime(
            resources.getString(
                R.string.time_alarm,
                TimeUtil.formatDateFromTimeStamp(alarm.time)
            )
        )
        
        // Show dialog safely
        try {
            if (!isFinishing && !isDestroyed) {
                alarmDialog!!.show(supportFragmentManager, "ALARM DIALOG")
            }
        } catch (e: Exception) {
            Log.e("AlarmActivity", "Error showing alarm dialog: ${e.message}", e)
        }
    }

    private fun loadAlarmsOnUI(alarmList: AlarmModelList?) {
        if (binding.alarmRlvAlarms.adapter == null) {
            setAlarmRecyclerView(alarmList!!)
        } else {
            val alarmAdapter = binding.alarmRlvAlarms.adapter as AlarmAdapter
            alarmAdapter.setAlarmList(alarmList!!)
            alarmAdapter.notifyDataSetChanged()
        }
    }

    private fun hideLoadingIcon() {
        binding.alarmLoadingIcon
//        binding.alarmFabAddAlarm.show()
    }

    private fun finishAlarm() {
        // Cancel the notification since we're using WorkManager now
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(AlarmConstant.ALARM_NOTIFICATION_ID)
        
        // Cancel any running WorkManager tasks for this alarm if needed
        androidx.work.WorkManager.getInstance(this).cancelAllWorkByTag("alarm_work")
    }

    private fun cancelAlarm(alarmId: Long) {
        val myIntent = Intent(this, AlarmReceiver::class.java)
        myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            alarmId.toInt(), myIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun showDetailAlarmActivity(alarm: AlarmModel) {
        val intent = Intent(this, AddEditAlarmActivity::class.java)
        intent.putExtra(AlarmConstant.KEY_ADD_EDIT_DETAIL_ALARM, AddEditAlarmActivity.DETAIL_ALARM)
        intent.putExtra(AlarmConstant.KEY_ALARM, Gson().toJson(alarm))
        startActivity(intent)
    }

    fun showPopupMenu(v: View) {
        val alarmModel = v.tag as AlarmModel
        val wrapper: Context = ContextThemeWrapper(this, R.style.popup_menu)
        val popupMenu = PopupMenu(wrapper, v)

        // Inflating the Popup using xml file
        popupMenu.getMenuInflater().inflate(
            R.menu.popup_menu_alarm,
            popupMenu.getMenu()
        )

        // Registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_detail -> showDetailAlarmActivity(alarmModel)
                R.id.menu_delete -> showDialogDeleteConfirmation(alarmModel)
            }
            false
        }
        popupMenu.show() // showing popup men
    }

    fun removeAlarmInRecyclerView(alarmModel: AlarmModel) {
        if (binding.alarmRlvAlarms.getAdapter() != null) {
            (binding.alarmRlvAlarms.getAdapter() as AlarmAdapter).removeAlarm(alarmModel.code)
        }
    }

    private fun showDialogDeleteConfirmation(alarm: AlarmModel) {
        val confirmDialog = ConfirmDialog()
        confirmDialog.setTitle(getString(R.string.delete_alarm))
        confirmDialog.setDescription(getString(R.string.delete_alarm_description, alarm.name))
        confirmDialog.setCallBackListener(object : ConfirmDialog.CallBackListener {
            override fun onYesClick() {
                viewModel.deleteAlarm(alarm.code)
                removeAlarmInRecyclerView(alarm)
                cancelAlarm(alarm.code)
                confirmDialog.dismissAllowingStateLoss()
            }

            override fun onNoClick() {
                confirmDialog.dismissAllowingStateLoss()
            }
        })
        confirmDialog.show(supportFragmentManager, confirmDialog.javaClass.simpleName)
    }

    private fun showAppInfo() {
        val infoDialog = InfoDialog()
        infoDialog.setMessage("")
        infoDialog.setDescription(
            resources.getString(
                R.string.app_version,
                AppUtil.getAppVersion(this)
            )
        )
        infoDialog.setDescription2(
            resources.getString(
                R.string.contact_email,
                "hoangphuc3117@gmail.com"
            )
        )
        infoDialog.setBtnName(getString(R.string.close))
        infoDialog.show(supportFragmentManager, infoDialog::class.java.simpleName)
    }

    private fun showLoadingIcon() {
        binding.alarmLoadingIcon.visibility = View.VISIBLE
//        binding.alarmFabAddAlarm.hide()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val alarmJson = intent?.getStringExtra(AlarmConstant.KEY_ALARM)
        if (alarmJson?.isNotEmpty() == true) {
            val alarm = Gson().fromJson(alarmJson, AlarmModel::class.java)
            showAlarm(alarm)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh alarm list when returning from AddEditAlarmActivity
        if (!firstStart) {
            viewModel.getAlarms()
        }
        firstStart = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver)
        }
    }
}