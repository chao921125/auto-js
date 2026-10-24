package org.autojs.app.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import org.autojs.app.R

class EditorFragment : Fragment() {

    private lateinit var editor: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editor = view.findViewById(R.id.editor)
        editor.typeface = android.graphics.Typeface.MONOSPACE
        editor.textSize = 14f
    }
}
