package ppapps.phapamnhacnho.modules.addeditalarm.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import ppapps.phapamnhacnho.R

class PlayTypeAdapter : BaseAdapter {
    companion object {
        const val PLAY_BEGINNING = 0
        const val PLAY_CONTINUE = 1
    }

    private lateinit var mPlayTypes: Array<String>
    private var mContext: Context? = null

    constructor(context: Context, playTypes: Array<String>) {
        this.mContext = context
        this.mPlayTypes = playTypes
    }

    override fun getCount(): Int = mPlayTypes.size

    override fun getItem(pos: Int): Any = mPlayTypes[pos]

    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(mContext)
                .inflate(R.layout.layout_spinner_play_type_items, parent, false)
            viewHolder = ViewHolder()
            viewHolder.tvAccountName =
                view!!.findViewById<View>(R.id.play_type_item_name) as TextView?
            view.setTag(viewHolder)
        }
        viewHolder = view.tag as ViewHolder
        viewHolder.tvAccountName!!.text = mPlayTypes[pos]
        return view
    }

    private class ViewHolder {
        var tvAccountName: TextView? = null
    }
}