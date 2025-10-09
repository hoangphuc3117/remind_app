package ppapps.phapamnhacnho.sharedmodule.customviews

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.Nullable
import ppapps.phapamnhacnho.basemodules.baseviews.BaseDialog
import ppapps.phapamnhacnho.databinding.DialogInfoBinding

class InfoDialog : BaseDialog() {

    private lateinit var binding: DialogInfoBinding

    private var mMessage: String? = null

    private var mDescription: String? = null

    private var mDescription2: String? = null

    private var mBtnName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        binding = DialogInfoBinding.inflate(inflater)
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
        if (TextUtils.isEmpty(mMessage)) {
            binding.dialogTvTitle.visibility = View.GONE
        } else {
            binding.dialogTvTitle.text = mMessage
        }
        if (TextUtils.isEmpty(mDescription)) {
            binding.dialogTvDescription.visibility = View.GONE
        } else {
            binding.dialogTvDescription.text = mDescription
        }
        if (TextUtils.isEmpty(mDescription2)) {
            binding.dialogTvDescription2.visibility = View.GONE
        } else {
            binding.dialogTvDescription2.text = mDescription2
        }
        if (!TextUtils.isEmpty(mBtnName)) {
            binding.dialogBtnOk.text = mBtnName
        }
        binding.dialogBtnOk.setOnClickListener { dismiss() }
    }

    fun setMessage(message: String?) {
        mMessage = message
    }

    fun setDescription(description: String?) {
        mDescription = description
    }

    fun setDescription2(description2: String?) {
        mDescription2 = description2
    }

    fun setBtnName(btnName: String?) {
        mBtnName = btnName
    }
}