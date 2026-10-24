package org.autojs.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R
import org.autojs.app.engine.LogEntryBus

class LogFragment : Fragment() {

    private lateinit var logView: TextView
    private val logBuilder = StringBuilder()
    private val maxLogLines = 1000

    private val logListener: (String, String) -> Unit = { level, msg ->
        requireActivity().runOnUiThread {
            val prefix = when (level) {
                "E" -> "[ERROR] "
                "W" -> "[WARN]  "
                "I" -> "[INFO]  "
                else -> "[$level]  "
            }
            val line = "$prefix$msg\n"
            logBuilder.append(line)
            // 限制日志行数
            val lines = logBuilder.toString().lines()
            if (lines.size > maxLogLines) {
                logBuilder.clear()
                lines.takeLast(maxLogLines).forEach { logBuilder.append(it).append('\n') }
            }
            logView.text = logBuilder.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logView = view.findViewById(R.id.log_view)
        logView.typeface = android.graphics.Typeface.MONOSPACE
        logView.textSize = 12f
        LogEntryBus.onLog(logListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LogEntryBus.offLog(logListener)
    }
}
