package org.autojs.app.script

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R
import java.text.SimpleDateFormat
import java.util.Locale

class ScriptListAdapter(
    private val listener: OnScriptActionListener
) : ListAdapter<ScriptManager.ScriptInfo, ScriptListAdapter.ViewHolder>(DIFF_CALLBACK) {

    interface OnScriptActionListener {
        fun onRun(path: String)
        fun onEdit(path: String)
        fun onDelete(path: String)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_script, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val script = getItem(position)
        holder.bind(script)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nameText: TextView = view.findViewById(R.id.script_name)
        private val infoText: TextView = view.findViewById(R.id.script_info)
        private val runBtn: View = view.findViewById(R.id.btn_run)
        private val moreBtn: View = view.findViewById(R.id.btn_more)

        fun bind(script: ScriptManager.ScriptInfo) {
            nameText.text = script.name
            val sizeKb = script.size / 1024.0
            infoText.text = "${String.format("%.1f KB", sizeKb)}  ·  ${dateFormat.format(script.lastModified)}"

            runBtn.setOnClickListener { listener.onRun(script.path) }

            itemView.setOnClickListener { listener.onEdit(script.path) }

            moreBtn.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.menu.add("编辑").setOnMenuItemClickListener { listener.onEdit(script.path); true }
                popup.menu.add("运行").setOnMenuItemClickListener { listener.onRun(script.path); true }
                popup.menu.add("删除").setOnMenuItemClickListener { listener.onDelete(script.path); true }
                popup.show()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScriptManager.ScriptInfo>() {
            override fun areItemsTheSame(a: ScriptManager.ScriptInfo, b: ScriptManager.ScriptInfo) = a.path == b.path
            override fun areContentsTheSame(a: ScriptManager.ScriptInfo, b: ScriptManager.ScriptInfo) = a == b
        }
    }
}
