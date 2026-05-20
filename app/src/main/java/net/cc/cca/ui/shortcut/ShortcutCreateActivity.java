package net.cc.cca.ui.shortcut;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import net.cc.cca.R;
import net.cc.cca.databinding.ShortcutCreateDialogBinding;
import net.cc.cca.external.ScriptIntents;
import net.cc.cca.external.shortcut.Shortcut;
import net.cc.cca.external.shortcut.ShortcutActivity;
import net.cc.cca.external.shortcut.ShortcutManager;
import net.cc.cca.model.script.ScriptFile;
import net.cc.cca.tool.BitmapTool;
import net.cc.cca.theme.dialog.ThemeColorMaterialDialogBuilder;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stardust on 2017/10/25.
 */

public class ShortcutCreateActivity extends AppCompatActivity {

    public static final String EXTRA_FILE = "file";
    private static final String LOG_TAG = "ShortcutCreateActivity";
    private ScriptFile mScriptFile;
    private boolean mIsDefaultIcon = true;
    private ShortcutCreateDialogBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScriptFile = (ScriptFile) getIntent().getSerializableExtra(EXTRA_FILE);
        showDialog();
    }

    private void showDialog() {
        binding = ShortcutCreateDialogBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        binding.name.setText(mScriptFile.getSimplifiedName());
        binding.icon.setOnClickListener(v -> selectIcon());
        new ThemeColorMaterialDialogBuilder(this)
                .customView(view, false)
                .title(R.string.text_send_shortcut)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> {
                    createShortcut();
                    finish();
                })
                .cancelListener(dialog -> finish())
                .show();
    }


    void selectIcon() {
        Intent intent = new Intent(this, ShortcutIconSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivityForResult(intent, 21209);
    }


    @SuppressLint("NewApi") //for fool android studio
    private void createShortcut() {
        createShortcutByShortcutManager();
    }

    private void createShortcutByShortcutManager() {
        Icon icon;
        if (mIsDefaultIcon) {
            icon = Icon.createWithResource(this, R.drawable.ic_file_type_js);
        } else {
            Bitmap bitmap = BitmapTool.drawableToBitmap(binding.icon.getDrawable());
            icon = Icon.createWithBitmap(bitmap);
        }
        PersistableBundle extras = new PersistableBundle(1);
        extras.putString(ScriptIntents.EXTRA_KEY_PATH, mScriptFile.getPath());
        Intent intent = new Intent(this, ShortcutActivity.class)
                .putExtra(ScriptIntents.EXTRA_KEY_PATH, mScriptFile.getPath())
                .setAction(Intent.ACTION_MAIN);

        ShortcutManager.getInstance(this).addPinnedShortcut(binding.name.getText(), mScriptFile.getPath(), icon, intent);
    }


    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        String packageName = data.getStringExtra(ShortcutIconSelectActivity.EXTRA_PACKAGE_NAME);
        if (packageName != null) {
            try {
                binding.icon.setImageDrawable(getPackageManager().getApplicationIcon(packageName));
                mIsDefaultIcon = false;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }
        Uri uri = data.getData();
        if (uri == null) {
            return;
        }
        Observable.fromCallable(() -> BitmapFactory.decodeStream(getContentResolver().openInputStream(uri)))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((bitmap -> {
                    binding.icon.setImageBitmap(bitmap);
                    mIsDefaultIcon = false;
                }), error -> {
                    Log.e(LOG_TAG, "decode stream", error);
                });
    }
}
