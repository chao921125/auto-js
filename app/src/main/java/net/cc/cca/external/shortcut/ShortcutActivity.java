package net.cc.cca.external.shortcut;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import net.cc.cca.model.script.PathChecker;
import net.cc.cca.external.ScriptIntents;
import net.cc.cca.model.script.ScriptFile;
import net.cc.cca.model.script.Scripts;

/**
 * Created by Stardust on 2017/1/23.
 */
public class ShortcutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String path = getIntent().getStringExtra(ScriptIntents.EXTRA_KEY_PATH);
        if (new PathChecker(this).checkAndToastError(path)) {
            runScriptFile(path);
        }
        finish();
    }

    private void runScriptFile(String path) {
        try {
            Scripts.INSTANCE.run(new ScriptFile(path));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            if (msg == null) {
                return;
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }

}
