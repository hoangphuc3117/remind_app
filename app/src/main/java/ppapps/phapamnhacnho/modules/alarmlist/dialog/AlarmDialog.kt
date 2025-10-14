package ppapps.phapamnhacnho.modules.alarmlist.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Nullable
import ppapps.phapamnhacnho.basemodules.baseviews.BaseDialog
import ppapps.phapamnhacnho.databinding.DialogAlarmBinding

class AlarmDialog : BaseDialog() {

    private lateinit var binding: DialogAlarmBinding

    private var message: String? = null

    private var time: String? = null

    private var btnName: String? = null

    var mCallBack: CallBackListener? = null

    fun setCallBackListener(callBackListener: CallBackListener?) {
        mCallBack = callBackListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogAlarmBinding.inflate(inflater)
        val rootView: View = binding.root
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        isCancelable = false
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    fun initView() {
        if (TextUtils.isEmpty(message)) {
            binding.dialogTvTitle.visibility = View.GONE
        } else {
            binding.dialogTvTitle.text = message
        }

        if (TextUtils.isEmpty(time)) {
            binding.dialogTvTime.visibility = View.GONE
        } else {
            binding.dialogTvTime.text = time
        }

        if (!TextUtils.isEmpty(btnName)) {
            binding.dialogBtnOk.text = btnName
        }
        binding.dialogBtnOk.setOnClickListener {
            dismiss()
            if (mCallBack != null) mCallBack!!.onDismiss()
        }
        binding.dialogBtnHide.setOnClickListener {
            dismiss()
            if (mCallBack != null) {
                mCallBack!!.onHide()
            }
        }
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun setTime(time: String?) {
        this.time = time
    }

    fun setBtnName(btnName: String?) {
        this.btnName = btnName
    }

    interface CallBackListener {
        fun onDismiss()
        fun onHide()
    }
}