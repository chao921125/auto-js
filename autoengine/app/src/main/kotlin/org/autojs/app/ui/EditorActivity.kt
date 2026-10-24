package org.autojs.app.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import org.autojs.app.R
import org.autojs.app.engine.EngineService
import org.autojs.app.script.ScriptManager

class EditorActivity : AppCompatActivity() {

    private lateinit var editor: EditText
    private var scriptPath: String? = null
    private val scriptManager get() = ScriptManager.get(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        setSupportActionBar(findViewById(R.id.toolbar))

        editor = findViewById(R.id.editor_edittext)
        editor.typeface = android.graphics.Typeface.MONOSPACE
        editor.textSize = 14f

        scriptPath = intent.getStringExtra("script_path")
        scriptPath?.let { path ->
            title = java.io.File(path).name
            editor.setText(scriptManager.readScript(path))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_run -> {
                saveAndRun()
                true
            }
            R.id.action_save -> {
                saveScript()
                true
            }
            R.id.action_format -> {
                // 代码格式化（P2）
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveScript() {
        scriptPath?.let { path ->
            scriptManager.writeScript(path, editor.text.toString())
        }
    }

    private fun saveAndRun() {
        saveScript()
        scriptPath?.let { EngineService.startScript(this, it) }
    }

    override fun onBackPressed() {
        saveScript()
        super.onBackPressed()
    }
}
