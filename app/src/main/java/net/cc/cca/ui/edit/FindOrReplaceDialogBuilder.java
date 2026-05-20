package net.cc.cca.ui.edit;

import android.content.Context;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import net.cc.cca.R;
import net.cc.cca.databinding.DialogFindOrReplaceBinding;
import net.cc.cca.theme.dialog.ThemeColorMaterialDialogBuilder;
import net.cc.cca.ui.edit.editor.CodeEditor;

/**
 * Created by Stardust on 2017/9/28.
 */

public class FindOrReplaceDialogBuilder extends ThemeColorMaterialDialogBuilder {

    private static final String KEY_KEYWORDS = "...";

    DialogFindOrReplaceBinding binding;
    private EditorView mEditorView;

    public FindOrReplaceDialogBuilder(@NonNull Context context, EditorView editorView) {
        super(context);
        mEditorView = editorView;
        setupViews();
        restoreState();
        autoDismiss(false);
        onNegative((dialog, which) -> dialog.dismiss());
        onPositive((dialog, which) -> {
            storeState();
            findOrReplace(dialog);
        });
    }

    private void setupViews() {
        binding = DialogFindOrReplaceBinding.inflate(android.view.LayoutInflater.from(context));
        View view = binding.getRoot();
        customView(view, true);
        positiveText(R.string.ok);
        negativeText(R.string.cancel);
        title(R.string.text_find_or_replace);
        
        binding.checkboxReplaceAll.setOnCheckedChangeListener((buttonView, isChecked) -> syncWithReplaceCheckBox());
        binding.replacement.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTextChanged();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }


    private void storeState() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit()
                .putString(KEY_KEYWORDS, binding.keywords.getText().toString())
                .apply();
    }


    private void restoreState() {
        binding.keywords.setText(PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(KEY_KEYWORDS, ""));
    }

    void syncWithReplaceCheckBox() {
        if (binding.checkboxReplaceAll.isChecked() && !binding.checkboxReplace.isChecked()) {
            binding.checkboxReplace.setChecked(true);
        }
    }

    void onTextChanged() {
        if (binding.replacement.getText().length() > 0) {
            binding.checkboxReplace.setChecked(true);
        }
    }

    private void findOrReplace(MaterialDialog dialog) {
        String keywords = mKeywordsEditText.getText().toString();
        if (keywords.isEmpty()) {
            return;
        }
        try {
            boolean usingRegex = binding.checkboxRegex.isChecked();
            if (!binding.checkboxReplace.isChecked()) {
                mEditorView.find(keywords, usingRegex);
            } else {
                String replacement = binding.replacement.getText().toString();
                if (binding.checkboxReplaceAll.isChecked()) {
                    mEditorView.replaceAll(keywords, replacement, usingRegex);
                } else {
                    mEditorView.replace(keywords, replacement, usingRegex);
                }
            }
            dialog.dismiss();
        } catch (CodeEditor.CheckedPatternSyntaxException e) {
            e.printStackTrace();
            mKeywordsEditText.setError(getContext().getString(R.string.error_pattern_syntax));
        }

    }

    public FindOrReplaceDialogBuilder setQueryIfNotEmpty(String s) {
        if (!TextUtils.isEmpty(s))
            binding.keywords.setText(s);
        return this;
    }
}
