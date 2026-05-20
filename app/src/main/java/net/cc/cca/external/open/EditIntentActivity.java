package net.cc.cca.external.open;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.widget.Toast;

import com.stardust.pio.PFiles;

import net.cc.cca.ui.BaseActivity;
import net.cc.cca.ui.edit.EditActivity;
import net.cc.cca.R;

import java.io.File;

/**
 * Created by Stardust on 2017/2/2.
 */

public class EditIntentActivity extends BaseActivity {

    private static final String EXTERNAL_FILES = "external_files";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            handleIntent();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.edit_and_run_handle_intent_error, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String path = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            path = uri.getPath();
        } else {
            int i = uri.getPath().indexOf(EXTERNAL_FILES);
            if (i >= 0) {
                path = uri.getPath().substring(i + EXTERNAL_FILES.length());
                if (!PFiles.exists(path)) {
                    // Android 10+ 使用 StorageUtil 获取路径
                    String basePath = net.cc.cca.tool.StorageUtil.getScriptDirPath(this);
                    path = new File(basePath, path).getPath();
                    if (!PFiles.exists(path)) {
                        path = null;
                    }
                }
            }
        }
        if (!TextUtils.isEmpty(path)) {
            EditActivity.editFile(this, path, false);
        } else {
            EditActivity.editFile(this, uri, false);
        }
    }
}
