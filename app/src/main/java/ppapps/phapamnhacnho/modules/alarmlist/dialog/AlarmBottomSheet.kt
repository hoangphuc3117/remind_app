package ppapps.phapamnhacnho.modules.alarmlist.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ppapps.phapamnhacnho.basemodules.util.TimeUtil
import ppapps.phapamnhacnho.databinding.BottomSheetAlarmBinding
import ppapps.phapamnhacnho.model.AlarmModel

class AlarmBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAlarmBinding? = null
    private val binding get() = _binding!!

    private var alarm: AlarmModel? = null
    private var alarmNumber: Int = 0
    private var onDetailClick: ((AlarmModel) -> Unit)? = null
    private var onDeleteClick: ((AlarmModel) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use Material 3 bottom sheet theme
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        alarm?.let { alarm ->
            // Set alarm preview
            binding.bsAlarmNumber.text = alarmNumber.toString()
            binding.bsAlarmName.text = alarm.name
            
            val time = TimeUtil.getTimeFromTimeStamp(alarm.time)
            val date = TimeUtil.getDateFromTimeStamp(alarm.time)
            binding.bsAlarmTime.text = "$time • $date"

            // Handle Detail button
            binding.bsBtnDetail.setOnClickListener {
                onDetailClick?.invoke(alarm)
                dismiss()
            }

            // Handle Delete button
            binding.bsBtnDelete.setOnClickListener {
                onDeleteClick?.invoke(alarm)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            alarm: AlarmModel,
            alarmNumber: Int,
            onDetail: (AlarmModel) -> Unit,
            onDelete: (AlarmModel) -> Unit
        ): AlarmBottomSheet {
            return AlarmBottomSheet().apply {
                this.alarm = alarm
                this.alarmNumber = alarmNumber
                this.onDetailClick = onDetail
                this.onDeleteClick = onDelete
            }
        }
    }
}
