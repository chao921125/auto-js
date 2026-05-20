package net.cc.cca.ui.common;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.stardust.app.DialogUtils;
import com.stardust.app.GlobalAppContext;

import net.cc.cca.R;
import net.cc.cca.databinding.DialogScriptLoopBinding;
import net.cc.cca.model.script.ScriptFile;
import net.cc.cca.model.script.Scripts;

/**
 * Created by Stardust on 2017/7/8.
 */

public class ScriptLoopDialog {

    private ScriptFile mScriptFile;
    private MaterialDialog mDialog;
    private DialogScriptLoopBinding binding;


    public ScriptLoopDialog(Context context, ScriptFile file) {
        mScriptFile = file;
        binding = DialogScriptLoopBinding.inflate(android.view.LayoutInflater.from(context));
        View view = binding.getRoot();
        mDialog = new MaterialDialog.Builder(context)
                .title(R.string.text_run_repeatedly)
                .customView(view, true)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> startScriptRunningLoop())
                .build();
    }

    private void startScriptRunningLoop() {
        try {
            int loopTimes = Integer.parseInt(binding.loopTimes.getText().toString());
            float loopInterval = Float.parseFloat(binding.loopInterval.getText().toString());
            float loopDelay = Float.parseFloat(binding.loopDelay.getText().toString());
            Scripts.INSTANCE.runRepeatedly(mScriptFile, loopTimes, (long) (1000L * loopDelay), (long) (loopInterval * 1000L));
        } catch (NumberFormatException e) {
            GlobalAppContext.toast(R.string.text_number_format_error);
        }
    }

    public ScriptLoopDialog windowType(int windowType) {
        Window window = mDialog.getWindow();
        if (window != null) {
            window.setType(windowType);
        }
        return this;
    }

    public void show() {
        DialogUtils.showDialog(mDialog);
    }

}
