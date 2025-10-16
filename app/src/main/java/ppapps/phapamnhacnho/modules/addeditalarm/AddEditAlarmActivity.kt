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
            if (uri != null) {
                // Take persistable URI permission for long-term access
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Permission may not be available, continue anyway
                }
                
                mEncodedURIMP3 = uri.toString() // Save URI as string
                
                // Get display name from URI
                var displayName: String? = null
                try {
                    // Try to get display name from content resolver
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (displayNameIndex != -1) {
                                displayName = cursor.getString(displayNameIndex)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Fallback to last path segment if display name not found
                if (displayName.isNullOrBlank()) {
                    displayName = uri.lastPathSegment ?: getString(R.string.add_alarm_choose_audio)
                }
                
                // Update button text to show selected file
                binding.addAlarmBrowseFile.text = displayName
                Toast.makeText(this, "Đã chọn: $displayName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val folderSelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                // Take persistable URI permission for long-term access
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Permission may not be available, continue anyway
                }
                
                mEncodedURIMP3 = uri.toString() // Save URI as string
                
                // Get display name from URI
                var displayName: String? = null
                try {
                    displayName = uri.lastPathSegment
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                if (displayName.isNullOrBlank()) {
                    displayName = getString(R.string.add_alarm_choose_audio)
                }
                
                // Update button text to show selected folder
                binding.addAlarmBrowseFile.text = displayName
                Toast.makeText(this, "Đã chọn thư mục: $displayName", Toast.LENGTH_SHORT).show()
            }
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
        binding.addAlarmEdtLoopTimes.isEnabled = false
        binding.addAlarmEdtTimeAlarm.isEnabled = false
        binding.addAlarmSpLoopType.isEnabled = false
        binding.addAlarmTvChooseDate.isEnabled = false
        binding.addAlarmTvChooseDate.isEnabled = false
        binding.addAlarmTvChooseTime.isEnabled = false
        binding.addAlarmIcCalendar.isEnabled = false
        binding.addAlarmIcClock.isEnabled = false
        binding.addAlarmBrowseFile.isEnabled = false
    }

    private fun enableViews() {
        binding.addAlarmEdtAlarmName.isEnabled = true
        binding.addAlarmEdtLoopTimes.isEnabled = true
        binding.addAlarmEdtTimeAlarm.isEnabled = true
        binding.addAlarmSpLoopType.isEnabled = true
        binding.addAlarmTvChooseDate.isEnabled = true
        binding.addAlarmTvChooseDate.isEnabled = true
        binding.addAlarmTvChooseTime.isEnabled = true
        binding.addAlarmIcCalendar.isEnabled = true
        binding.addAlarmIcClock.isEnabled = true
        binding.addAlarmBrowseFile.isEnabled = true
    }

    private fun initDataForDetailAlarm() {
        mOldAlarm =
            Gson().fromJson(intent.getStringExtra(AlarmConstant.KEY_ALARM), AlarmModel::class.java)
        binding.addAlarmEdtAlarmName.setText(mOldAlarm?.name)
        binding.addAlarmTvChooseDate.text = TimeUtil.getDateFromTimeStamp(mOldAlarm!!.time)
        binding.addAlarmTvChooseTime.text = TimeUtil.getTimeFromTimeStamp(mOldAlarm!!.time)
        
        if (!TextUtils.isEmpty(mOldAlarm!!.uriFileFolder)) {
            mEncodedURIMP3 = mOldAlarm!!.uriFileFolder
            // Show file name on button
            try {
                val uri = android.net.Uri.parse(mEncodedURIMP3)
                
                // Try to get display name from content resolver
                var displayName: String? = null
                try {
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (displayNameIndex != -1) {
                                displayName = cursor.getString(displayNameIndex)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                // Fallback to last path segment
                if (displayName.isNullOrBlank()) {
                    displayName = uri.lastPathSegment
                }
                
                // Show display name or default text
                if (!displayName.isNullOrBlank()) {
                    binding.addAlarmBrowseFile.text = displayName
                } else {
                    binding.addAlarmBrowseFile.text = getString(R.string.add_alarm_choose_audio)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.addAlarmBrowseFile.text = getString(R.string.add_alarm_choose_audio)
            }
        }
        
        binding.addAlarmSpLoopType.setSelection(mOldAlarm!!.loopType)
        binding.addAlarmEdtLoopTimes.setText(mOldAlarm!!.loopTime.toString())
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

        setupDataForPlayType()
        setupDataForLoopType()

        binding.addAlarmTvChooseDate.setOnClickListener {
            val dpd = DatePickerDialog(this, this, mYear, mMonth, mDay)
            dpd.show()
        }

        binding.addAlarmBrowseFile.setOnClickListener {
            showFileChooser()
        }

        binding.addAlarmTvChooseTime.setOnClickListener {
            val tpd = TimePickerDialog(this, this, mHour, mMinute, true)
            tpd.show()
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Storage permission handling removed - using SAF instead
    }

    private fun addNewAlarm() {
        Log.d("AddAlarm", "addNewAlarm called")
        val alarm = AlarmModel()
        val date = TimeUtil.formatDate(mDay.toString(), (mMonth + 1).toString(), mYear.toString())
        val time = TimeUtil.formatTime(mHour.toString(), mMinute.toString())
        mDateTime = TimeUtil.convertDateTimeToTimeStamp(date, time)
        
        Log.d("AddAlarm", "Before compareTime: mDateTime=$mDateTime (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(mDateTime))})")
        
        // Compare with current time to avoid setting alarm in the past
        mDateTime = compareTimeWithCurrentTime(mDateTime)
        
        Log.d("AddAlarm", "After compareTime: mDateTime=$mDateTime (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(mDateTime))})")
        Log.d("AddAlarm", "Current time: ${System.currentTimeMillis()} (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(System.currentTimeMillis()))})")
        
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
        val loopType: Int = binding.addAlarmSpLoopType.selectedItemPosition
        alarm.loopType = loopType

        Log.d("AddAlarm", "Created alarm: name=${alarm.name}, time=${alarm.time}, uri=${alarm.uriFileFolder}")
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
        val loopType: Int = binding.addAlarmSpLoopType.selectedItemPosition
        mNewAlarm?.loopType = loopType
        
        // Preserve fields from old alarm that are not editable in UI
        mNewAlarm?.fileIndex = mOldAlarm?.fileIndex ?: 0
        mNewAlarm?.playingPosition = mOldAlarm?.playingPosition ?: 0

        Log.d("EditAlarm", "Updating alarm: code=${mNewAlarm?.code}, name=${mNewAlarm?.name}, time=${mNewAlarm?.time}, uri=${mNewAlarm?.uriFileFolder}")
        viewModel.updateAlarm(mNewAlarm)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setAlarm(id: Long) {
        Log.d("AddAlarm", "setAlarm called with id=$id")
        
        if (id <= 0) {
            Log.e("AddAlarm", "Invalid alarm ID: $id")
            Toast.makeText(this, "Failed to create alarm", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        if (alarmManager == null) {
            alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        }

        // Check if we can schedule exact alarms on Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager!!.canScheduleExactAlarms()) {
                // Request user to grant permission
                Log.w("AddAlarm", "Cannot schedule exact alarms, requesting permission")
                Toast.makeText(this, "Please grant 'Alarms & reminders' permission", Toast.LENGTH_LONG).show()
                val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                finish()
                return
            }
        }

        Log.d("AddAlarm", "Setting new alarm: id=$id, time=$mDateTime (${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(mDateTime))})")
        
        // Check if alarm time is in the future
        val currentTime = System.currentTimeMillis()
        if (mDateTime <= currentTime) {
            Log.w("AddAlarm", "Alarm time is in the past or current! alarm=$mDateTime, current=$currentTime")
            // Still schedule it, but it may trigger immediately
        } else {
            val diff = mDateTime - currentTime
            val minutes = diff / 60000
            Log.d("AddAlarm", "Alarm will trigger in $minutes minutes")
        }
        
        val myIntent = Intent(this, AlarmReceiver::class.java)
        myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, id)
        // IMPORTANT: Use unique requestCode (alarm id) for each alarm
        val pendingIntent = PendingIntent.getBroadcast(
            this, 
            id.toInt(), 
            myIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            alarmManager!!.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mDateTime, pendingIntent)
            Log.d("AddAlarm", "✓ Alarm scheduled successfully via AlarmManager")
            Toast.makeText(this, "Alarm set successfully", Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Log.e("AddAlarm", "SecurityException: Cannot schedule alarm - ${e.message}", e)
            Toast.makeText(this, "Permission denied: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("AddAlarm", "Failed to schedule alarm: ${e.message}", e)
            Toast.makeText(this, "Failed to set alarm: ${e.message}", Toast.LENGTH_LONG).show()
        }
        
        finish()
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            // Allow multiple MIME types for better compatibility
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/mpeg", "audio/mp3", "audio/*"))
        }
        try {
            fileSelectLauncher.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            // Fallback to older method
            val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            try {
                fileSelectLauncher.launch(Intent.createChooser(fallbackIntent, "Select an Audio File"))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showFolderChooser() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // For folder selection, use OPEN_DOCUMENT_TREE
        }
        try {
            folderSelectLauncher.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            // Fallback to file picker for older devices
            val fallbackIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/mpeg", "audio/mp3", "audio/*"))
            }
            try {
                folderSelectLauncher.launch(fallbackIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun handleAfterUpdatingAlarm(updateAlarm: Boolean) {
        Log.d("EditAlarm", "handleAfterUpdatingAlarm called: updateAlarm=$updateAlarm, mNewAlarm=${mNewAlarm?.code}")
        if (mNewAlarm == null) {
            Log.e("EditAlarm", "mNewAlarm is null, cannot update alarm")
            return
        }
        if (updateAlarm) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            
            // Check if we can schedule exact alarms on Android 12+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Request user to grant permission
                    Log.w("EditAlarm", "Cannot schedule exact alarms, requesting permission")
                    val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                    finish()
                    return
                }
            }
            
            Log.d("EditAlarm", "Setting alarm via AlarmManager: code=${mNewAlarm?.code}, time=$mDateTime")
            val myIntent = Intent(this, AlarmReceiver::class.java)
            myIntent.putExtra(AlarmConstant.KEY_ALARM_CODE, mNewAlarm?.code)
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                mNewAlarm?.code?.toInt() ?: 0,
                myIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mDateTime, pendingIntent)
            Log.d("EditAlarm", "Alarm scheduled successfully")
            
            // Optional: Send broadcast to refresh AlarmActivity list
            // val refreshIntent = Intent("ppapps.phapamnhacnho.refreshalarms")
            // sendBroadcast(refreshIntent)
        } else {
            Log.e("EditAlarm", "Update alarm failed, updateAlarm=$updateAlarm")
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
            // If the alarm time is in the past, schedule it for the next occurrence
            // by adding 1 day (24 hours * 60 minutes * 60 seconds * 1000 milliseconds)
            timeStamp + (24 * 60 * 60 * 1000)
        } else {
            timeStamp
        }
    }
}