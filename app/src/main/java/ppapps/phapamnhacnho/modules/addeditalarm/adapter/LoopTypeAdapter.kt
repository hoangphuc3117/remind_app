package ppapps.phapamnhacnho.modules.addeditalarm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ppapps.phapamnhacnho.R

class LoopTypeAdapter() : BaseAdapter() {
    companion object {
        var NO_LOOP = 0
        var LOOP_DAY = 1
        var LOOP_WEEK = 2
        var LOOP_MONTH = 3
    }

    private lateinit var mLoopTypes: Array<String>
    private var mContext: Context? = null

    constructor(context: Context?, loopTypes: Array<String>) : this() {
        mContext = context
        mLoopTypes = loopTypes
    }

    override fun getCount(): Int = mLoopTypes.size

    override fun getItem(pos: Int) = mLoopTypes[pos]

    override fun getItemId(pos: Int) = pos.toLong()

    override fun getView(pos: Int, convertView: View?, p2: ViewGroup?): View {
        var view: View? = convertView
        var viewHolder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_spinner_play_type_items, p2, false)
            viewHolder = ViewHolder()
            viewHolder.tvAccountName =
                view!!.findViewById<View>(R.id.play_type_item_name) as TextView?
            view.setTag(viewHolder)
        }
        viewHolder = view.tag as ViewHolder
        viewHolder.tvAccountName!!.text = mLoopTypes[pos]
        return view
    }

    private class ViewHolder {
        var tvAccountName: TextView? = null
    }
}