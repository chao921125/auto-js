package org.autojs.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.autojs.app.R
import org.autojs.app.engine.EngineService
import org.autojs.app.script.ScriptManager
import org.autojs.app.script.ScriptListAdapter
import org.autojs.app.script.ScriptListAdapter.OnScriptActionListener
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ScriptListFragment : Fragment(), OnScriptActionListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScriptListAdapter
    private val scriptManager get() = ScriptManager.get(requireContext())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_script_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.script_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScriptListAdapter(this)
        recyclerView.adapter = adapter

        adapter.submitList(scriptManager.getAllScripts())

        view.findViewById<FloatingActionButton>(R.id.fab_new_script).setOnClickListener {
            showNewScriptDialog()
        }
    }

    override fun onRun(path: String) {
        EngineService.startScript(requireContext(), path)
    }

    override fun onEdit(path: String) {
        val intent = android.content.Intent(requireContext(), EditorActivity::class.java)
        intent.putExtra("script_path", path)
        startActivity(intent)
    }

    override fun onDelete(path: String) {
        scriptManager.deleteScript(path)
        adapter.submitList(scriptManager.getAllScripts())
    }

    private fun showNewScriptDialog() {
        val editText = android.widget.EditText(requireContext()).apply {
            hint = "脚本名称.js"
        }
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("新建脚本")
            .setView(editText)
            .setPositiveButton("创建") { _, _ ->
                val name = editText.text.toString().ifBlank { "untitled.js" }
                scriptManager.createScript(name)
                adapter.submitList(scriptManager.getAllScripts())
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
