package ppapps.phapamnhacnho.sharedmodule.customviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Nullable
import ppapps.phapamnhacnho.basemodules.baseviews.BaseDialog
import ppapps.phapamnhacnho.databinding.DialogConfirmBinding

class ConfirmDialog : BaseDialog() {
    private lateinit var binding: DialogConfirmBinding
    private var title: String? = null
    private var description: String? = null
    private var listener: CallBackListener? = null

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        binding = DialogConfirmBinding.inflate(inflater, container, false)
        val rootView: View = binding.root
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        isCancelable = false
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.dialogConfirmTvTitle.text = title
        binding.dialogConfirmTvDescription.text = description

        binding.dialogConfirmBtnCancel.setOnClickListener {
            listener?.onNoClick()
        }

        binding.dialogConfirmBtnOk.setOnClickListener {
            listener?.onYesClick()
        }
    }

    fun setCallBackListener(callBackListener: CallBackListener?) {
        listener = callBackListener
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    interface CallBackListener {
        fun onYesClick()
        fun onNoClick()
    }
}