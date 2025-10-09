package ppapps.phapamnhacnho.modules.alarmlist

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import androidx.appcompat.widget.PopupMenu
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
        binding = ActAlarmBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)

        initView()
        initData()
    }

    private fun initView() {
        initToolbar()
        initReceiver()
        initEventListener()
        initListener()
    }

    private fun initEventListener() {
        binding.alarmToolbar.toolbarIvIconRight.setOnClickListener {
            showAppInfo()
        }

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
        binding.alarmToolbar.toolbarTvTitle.text = getString(R.string.title_alarm)
        binding.alarmToolbar.toolbarIvIconLeft.setImageResource(R.drawable.ic_chevron_left_white_24dp)
        binding.alarmToolbar.toolbarIvIconLeft.visibility = View.GONE
        binding.alarmToolbar.toolbarIvIconRight.setImageResource(R.drawable.ic_info_50)
        binding.alarmToolbar.toolbarIvIconRight.visibility = View.VISIBLE
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
                    //if (mPresenter != null) mPresenter.getAlarms()
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
        if (alarmDialog == null) {
            alarmDialog = AlarmDialog()
        }
        if (!alarmDialog!!.isVisible) {
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
            alarmDialog!!.show(supportFragmentManager, "ALARM DIALOG")
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
        val intent = Intent(this, AlarmService::class.java)
        intent.action = AlarmService.ACTION_STOP
        startService(intent)
    }

    private fun cancelAlarm(alarmId: Long) {
        val myIntent = Intent(this, AlarmReceiver::class.java)
        myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, alarmId)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            alarmId.toInt(), myIntent, 0
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
}