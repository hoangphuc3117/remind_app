package ppapps.phapamnhacnho.sharedmodule.customviews

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import ppapps.phapamnhacnho.R
import ppapps.phapamnhacnho.databinding.FrgLoadingBinding

class LoadingFragment : DialogFragment() {
    val TAG = LoadingFragment::class.java.simpleName
    private val STYLE_SPINNER = 0

    private var mStyle = STYLE_SPINNER

    private lateinit var binding: FrgLoadingBinding

    companion object {
        fun newInstance(): LoadingFragment {
            return LoadingFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FrgLoadingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.vLoadingBackground.visibility = View.VISIBLE
        binding.pbLoading.visibility = View.VISIBLE
    }

    fun getStyle(): Int {
        return mStyle
    }

    fun setStyle(style: Int) {
        this.mStyle = style
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // the content
        val root = RelativeLayout(activity)
        root.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // creating the fullscreen dialog
        val dialog = context?.let { Dialog(it) }
        dialog?.window!!.attributes.windowAnimations = R.style.LoadingDialogTheme
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return dialog
    }
}