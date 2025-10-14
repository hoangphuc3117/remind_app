package ppapps.phapamnhacnho.modules.addeditalarm

import android.app.*
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.basemodules.baseviews.BaseActivity
import ppapps.phapamnhacnho.basemodules.util.PathUtil
import ppapps.phapamnhacnho.basemodules.util.TimeUtil
import ppapps.phapamnhacnho.constant.AlarmConstant
import ppapps.phapamnhacnho.databinding.ActAddAlarmBinding
import ppapps.phapamnhacnho.model.AlarmModel
import ppapps.phapamnhacnho.modules.addeditalarm.adapter.LoopTypeAdapter
import ppapps.phapamnhacnho.modules.addeditalarm.adapter.PlayTypeAdapter
import ppapps.phapamnhacnho.modules.alarmreceiver.AlarmReceiver
import ppapps.phapamnhacnho.modules.mediaplayer.MyPlayer
import java.util.*
import android.os.Build

import android.app.AlarmManager
import android.app.PendingIntent







class AddEditAlarmActivity : BaseActivity(),
    OnDateSetListener,
    OnTimeSetListener,
    CompoundButton.OnCheckedChangeListener {
    companion object {
        const val ADD_ALARM = 100
        const val EDIT_ALARM = 101
        const val DETAIL_ALARM = 102
        // Request codes no longer needed with modern ActivityResultLauncher approach
    }

    private lateinit var viewModel : AddEditAlarmViewModel

    private lateinit var binding: ActAddAlarmBinding

    // Modern Activity Result Launchers
    private val fileSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            mEncodedURIMP3 = PathUtil.getPath(this, uri)
            binding.addAlarmTvChooseFile.text = mEncodedURIMP3
        } else {
            binding.addAlarmTvChooseFile.text = getString(R.string.add_alarm_select_file_or_folder)
        }
    }

    private val folderSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            mEncodedURIMP3 = PathUtil.getPath(this, uri)
            mEncodedURIMP3 = PathUtil.getFolderPathFromFilePath(mEncodedURIMP3)
            binding.addAlarmTvChooseFile.text = mEncodedURIMP3
        } else {
            binding.addAlarmTvChooseFile.text = getString(R.string.add_alarm_select_file_or_folder)
        }
    }

    var alarmManager: AlarmManager? = null

    private var mHour = 0

    private var mMinute = 0

    private val mSecond = 0

    private var mDay = 0

    private var mMonth = 0

    private var mYear = 0

    private var mDateTime: Long = 0

    private var mEncodedURIMP3: String? = null

    private var mCheckedFile = true

    private var mAlarmType = 0

    private var mOldAlarm: AlarmModel? = null

    private var mNewAlarm: AlarmModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActAddAlarmBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        viewModel = ViewModelProvider(this).get(AddEditAlarmViewModel::class.java)

        // Set up modern back navigation handling
        setupBackPressedCallback()

        // Set status bar
        initViews()
        mAlarmType = intent.getIntExtra(AlarmConstant.KEY_ADD_EDIT_DETAIL_ALARM, -1)
        if (mAlarmType == DETAIL_ALARM) {
            initialForDetailAlarm()
        } else {
            initialForAddAlarm()
        }
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mAlarmType == EDIT_ALARM) {
                    initialForDetailAlarm()
                } else {
                    // Let the system handle the back press
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initialForAddAlarm() {
        initToolbar()
        initData()
    }

    private fun initialForDetailAlarm() {
        mAlarmType = DETAIL_ALARM
        initToolbarForDetailAlarm()
        initDataForDetailAlarm()
        disableViews()
    }

    private fun initialForEditAlarm() {
        mAlarmType = EDIT_ALARM
        initToolbarForEditAlarm()
        enableViews()
    }

    private fun disableViews() {
        binding.addAlarmEdtAlarmName.isEnabled = false
        binding.addAlarmCbFileType.isEnabled = false
        binding.addAlarmEdtLoopTimes.isEnabled = false
        binding.addAlarmEdtTimeAlarm.isEnabled = false
        binding.addAlarmSpAlarmType.isEnabled = false
        binding.addAlarmSpLoopType.isEnabled = false
        binding.addAlarmTvChooseDate.isEnabled = false
        binding.addAlarmTvChooseDate.isEnabled = false
        binding.addAlarmTvChooseTime.isEnabled = false
        binding.addAlarmTvChooseFile.isEnabled = false
        binding.addAlarmIcCalendar.isEnabled = false
        binding.addAlarmIcClock.isEnabled = false
        binding.addAlarmBrowseFile.isEnabled = false
    }

    private fun enableViews() {
        binding.addAlarmEdtAlarmName.isEnabled = true
        binding.addAlarmCbFileType.isEnabled = true
        binding.addAlarmEdtLoopTimes.isEnabled = true
        binding.addAlarmEdtTimeAlarm.isEnabled = true
        binding.addAlarmSpAlarmType.isEnabled = true
        binding.addAlarmSpLoopType.isEnabled = true
        binding.addAlarmTvChooseDate.isEnabled = true
        binding.addAlarmTvChooseDate.isEnabled = true
        binding.addAlarmTvChooseTime.isEnabled = true
        binding.addAlarmTvChooseFile.isEnabled = true
        binding.addAlarmIcCalendar.isEnabled = true
        binding.addAlarmIcClock.isEnabled = true
        binding.addAlarmBrowseFile.isEnabled = true
    }

    private fun initDataForDetailAlarm() {
        mOldAlarm =
            Gson().fromJson(intent.getStringExtra(AlarmConstant.KEY_ALARM), AlarmModel::class.java)
        binding.addAlarmEdtAlarmName.setText(mOldAlarm?.name)
        binding.addAlarmCbFileType.isChecked = mOldAlarm!!.fileType == MyPlayer.PLAY_FOLDER
        binding.addAlarmTvChooseDate.text = TimeUtil.getDateFromTimeStamp(mOldAlarm!!.time)
        binding.addAlarmTvChooseTime.text = TimeUtil.getTimeFromTimeStamp(mOldAlarm!!.time)
        if (TextUtils.isEmpty(mOldAlarm!!.uriFileFolder)) {
            binding.addAlarmTvChooseFile.text = getString(R.string.add_alarm_select_file_or_folder)
        } else {
            mEncodedURIMP3 = mOldAlarm!!.uriFileFolder
            binding.addAlarmTvChooseFile.text = mEncodedURIMP3
        }
        binding.addAlarmSpLoopType.setSelection(mOldAlarm!!.loopType)
        binding.addAlarmEdtLoopTimes.setText(mOldAlarm!!.loopTime.toString())
        binding.addAlarmSpAlarmType.setSelection(mOldAlarm!!.playType)
        binding.addAlarmEdtTimeAlarm.setText(mOldAlarm!!.timeAlarm.toString())

        //Init time
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = mOldAlarm!!.time
        mYear = calendar[Calendar.YEAR]
        mMonth = calendar[Calendar.MONTH]
        mDay = calendar[Calendar.DAY_OF_MONTH]
        mHour = calendar[Calendar.HOUR_OF_DAY]
        mMinute = calendar[Calendar.MINUTE]
        mDateTime = calendar.timeInMillis
        binding.addAlarmTvChooseDate.setText(TimeUtil.getDateFromTimeStamp(mDateTime))
        binding.addAlarmTvChooseTime.setText(TimeUtil.getTimeFromTimeStamp(mDateTime))
    }

    private fun initViews() {
        binding.toolbar.toolbarIvIconLeft.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.toolbarIvIconRight.setOnClickListener {
            if (mAlarmType == EDIT_ALARM) {
                editAlarm()
            } else if (mAlarmType == DETAIL_ALARM) {
                initialForEditAlarm()
            } else {
                addNewAlarm()
            }
        }

        binding.addAlarmCbFileType.setOnCheckedChangeListener(this)
        setupDataForPlayType()
        setupDataForLoopType()

        binding.addAlarmTvChooseDate.setOnClickListener {
            val dpd = DatePickerDialog(this, this, mYear, mMonth, mDay)
            dpd.show()
        }

        binding.addAlarmTvChooseTime.setOnClickListener {
            val tpd = TimePickerDialog(this, this, mHour, mMinute, true)
            tpd.show()
        }

        binding.addAlarmTvChooseFile.setOnClickListener {
            if (isStoragePermissionGranted()) {
                if (mCheckedFile) {
                    showFileChooser()
                } else {
                    showFolderChooser()
                }
            }
        }

        viewModel.liveDataSetAlarmResult.observe(this, {
            setAlarm(it)
        })

        viewModel.liveDataUpdateAlarmResult.observe(this, {
            handleAfterUpdatingAlarm(it)
        })
    }

    private fun setupDataForPlayType() {
        val playTypes = resources.getStringArray(R.array.alarm_play_type)
        val playTypeAdapter = PlayTypeAdapter(this, playTypes)
        binding.addAlarmSpAlarmType.adapter = playTypeAdapter
    }

    private fun setupDataForLoopType() {
        val loopTypes = resources.getStringArray(R.array.alarm_loop_type)
        val loopTypeAdapter = LoopTypeAdapter(this, loopTypes)
        binding.addAlarmSpLoopType.adapter = loopTypeAdapter
    }

    private fun initToolbar() {
        binding.toolbar.toolbarTvTitle.text = getString(R.string.title_add_alarm)
        binding.toolbar.toolbarIvIconLeft.setImageResource(R.drawable.ic_chevron_left_white_24dp)
        binding.toolbar.toolbarIvIconLeft.visibility = View.VISIBLE
        binding.toolbar.toolbarIvIconRight.setImageResource(R.drawable.ic_done_white_24dp)
        binding.toolbar.toolbarIvIconRight.visibility = View.VISIBLE
    }

    private fun initToolbarForDetailAlarm() {
        binding.toolbar.toolbarTvTitle.text = getString(R.string.title_detail_alarm)
        binding.toolbar.toolbarIvIconLeft.visibility = View.VISIBLE
        binding.toolbar.toolbarIvIconLeft.setImageResource(R.drawable.ic_chevron_left_white_24dp)
        binding.toolbar.toolbarIvIconRight.visibility = View.VISIBLE
        binding.toolbar.toolbarIvIconRight.setImageResource(R.drawable.ic_edit_48)
    }

    private fun initToolbarForEditAlarm() {
        binding.toolbar.toolbarTvTitle.text = getString(R.string.title_edit_alarm)
        binding.toolbar.toolbarIvIconRight.visibility = View.VISIBLE
        binding.toolbar.toolbarIvIconRight.setImageResource(R.drawable.ic_done_white_24dp)
    }

    private fun initData() {
        val calendar = Calendar.getInstance()
        mYear = calendar[Calendar.YEAR]
        mMonth = calendar[Calendar.MONTH]
        mDay = calendar[Calendar.DAY_OF_MONTH]
        mHour = calendar[Calendar.HOUR_OF_DAY]
        mMinute = calendar[Calendar.MINUTE]
        mDateTime = calendar.timeInMillis
        binding.addAlarmTvChooseDate.text = TimeUtil.getDateFromTimeStamp(mDateTime)
        binding.addAlarmTvChooseTime.text = TimeUtil.getTimeFromTimeStamp(mDateTime)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE -> {
                val grantedWriteExternalStorage =
                    grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (grantedWriteExternalStorage) {
                    if (mCheckedFile) {
                        showFileChooser()
                    } else {
                        showFolderChooser()
                    }
                } else {
                    Log.d(this.localClassName, "Permission: denied request external storage")
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun addNewAlarm() {
        val alarm = AlarmModel()
        val date = TimeUtil.formatDate(mDay.toString(), (mMonth + 1).toString(), mYear.toString())
        val time = TimeUtil.formatTime(mHour.toString(), mMinute.toString())
        mDateTime = TimeUtil.convertDateTimeToTimeStamp(date, time)
        alarm.time = mDateTime
        alarm.name = binding.addAlarmEdtAlarmName.text.toString()
        alarm.status = AlarmModel.STATUS_IN_PROGRESS
        alarm.uriFileFolder = mEncodedURIMP3
        var timeAlarm = AlarmConstant.ALARM_TIME_DEFAULT.toLong()
        if ("" != binding.addAlarmEdtTimeAlarm.text.toString()) {
            timeAlarm = binding.addAlarmEdtTimeAlarm.text.toString().toLong()
        }
        alarm.timeAlarm = timeAlarm
        if (mCheckedFile) {
            alarm.fileType = MyPlayer.PLAY_FILE
        } else {
            alarm.fileType = MyPlayer.PLAY_FOLDER
        }
        var loopTimes = 1
        val strLoopTimes: String = binding.addAlarmEdtLoopTimes.text.toString()
        if ("" != strLoopTimes && "0" != strLoopTimes) {
            loopTimes = Integer.valueOf(strLoopTimes)
        }
        alarm.loopTime = loopTimes
        alarm.countLoopTimes = 1
        val playType: Int = binding.addAlarmSpAlarmType.selectedItemPosition
        alarm.playType = playType
        val loopType: Int = binding.addAlarmSpLoopType.selectedItemPosition
        alarm.loopType = loopType

        viewModel.addAlarm(alarm)
    }

    private fun editAlarm() {
        mNewAlarm = AlarmModel()
        mNewAlarm?.code = mOldAlarm!!.code
        mNewAlarm?.name = binding.addAlarmEdtAlarmName.text.toString()
        val date = TimeUtil.formatDate(mDay.toString(), (mMonth + 1).toString(), mYear.toString())
        val time = TimeUtil.formatTime(mHour.toString(), mMinute.toString())
        mDateTime = compareTimeWithCurrentTime(TimeUtil.convertDateTimeToTimeStamp(date, time))
        mNewAlarm?.time = mDateTime
        mNewAlarm?.status = AlarmModel.STATUS_IN_PROGRESS
        mNewAlarm?.uriFileFolder = mEncodedURIMP3
        var timeAlarm = AlarmConstant.ALARM_TIME_DEFAULT.toLong()
        if ("" != binding.addAlarmEdtTimeAlarm.text.toString()) {
            timeAlarm = java.lang.Long.valueOf(binding.addAlarmEdtTimeAlarm.text.toString())
        }
        mNewAlarm?.timeAlarm = timeAlarm
        if (mCheckedFile) {
            mNewAlarm?.fileType = MyPlayer.PLAY_FILE
        } else {
            mNewAlarm?.fileType = MyPlayer.PLAY_FOLDER
        }
        var loopTimes = 1
        val strLoopTimes: String = binding.addAlarmEdtLoopTimes.getText().toString()
        if ("" != strLoopTimes && "0" != strLoopTimes) {
            loopTimes = Integer.valueOf(strLoopTimes)
        }
        mNewAlarm?.loopTime = loopTimes
        mNewAlarm?.countLoopTimes = 1
        val playType: Int = binding.addAlarmSpAlarmType.selectedItemPosition
        mNewAlarm?.playType = playType
        val loopType: Int = binding.addAlarmSpLoopType.selectedItemPosition
        mNewAlarm?.loopType = loopType

        viewModel.updateAlarm(mNewAlarm)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setAlarm(id: Long) {
        if (alarmManager == null) {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        }

        val myIntent = Intent(this, AlarmReceiver::class.java)
        myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, id)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager!!.set(AlarmManager.RTC_WAKEUP, mDateTime, pendingIntent)
        finish()
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/mpeg"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            fileSelectLauncher.launch(Intent.createChooser(intent, "Select a File"))
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                this, "Please install a File Manager.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showFolderChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "file/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            folderSelectLauncher.launch(Intent.createChooser(intent, "Select a File"))
        } catch (ex: ActivityNotFoundException) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(
                this, "Please install a File Manager.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun handleAfterUpdatingAlarm(updateAlarm: Boolean) {
        if (mNewAlarm == null) {
            return
        }
        if (updateAlarm) {
            val myIntent = Intent(this, AlarmReceiver::class.java)
            myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, mNewAlarm?.code)
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                mNewAlarm?.code?.toInt() ?: 0,
                myIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager[AlarmManager.RTC_WAKEUP, mDateTime] = pendingIntent
        }
        finish()
    }

    override fun onCheckedChanged(compoundButton: CompoundButton?, checked: Boolean) {
        mCheckedFile = !checked
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mDay = dayOfMonth
        mMonth = month
        mYear = year
        binding.addAlarmTvChooseDate.text = TimeUtil.formatDate(
            dayOfMonth.toString(),
            (mMonth + 1).toString(),
            mYear.toString()
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mHour = hourOfDay
        mMinute = minute
        binding.addAlarmTvChooseTime.text = TimeUtil.formatTime(
            mHour.toString(),
            mMinute.toString()
        )
    }

    private fun compareTimeWithCurrentTime(timeStamp: Long): Long {
        return if (TimeUtil.isTimeBeforeCurrentTime(timeStamp)) {
            TimeUtil.getCurrentDateTimeStamp()
        } else {
            timeStamp
        }
    }
}