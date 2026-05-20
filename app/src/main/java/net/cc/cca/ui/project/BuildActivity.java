package net.cc.cca.ui.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.textfield.TextInputLayout;
import net.cc.stardust.project.ProjectConfig;
import com.stardust.util.IntentUtil;

import net.cc.cca.Pref;
import net.cc.cca.R;
import net.cc.cca.databinding.ActivityBuildBinding;
import net.cc.cca.build.ApkBuilder;
import net.cc.cca.build.ApkBuilderPluginHelper;
import net.cc.cca.external.fileprovider.AppFileProvider;
import net.cc.cca.model.script.ScriptFile;
import net.cc.cca.theme.dialog.ThemeColorMaterialDialogBuilder;
import net.cc.cca.tool.BitmapTool;
import net.cc.cca.ui.BaseActivity;
import net.cc.cca.ui.filechooser.FileChooserDialogBuilder;
import net.cc.cca.ui.shortcut.ShortcutIconSelectActivity;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stardust on 2017/10/22.
 */
public class BuildActivity extends BaseActivity implements ApkBuilder.ProgressCallback {

    private static final int REQUEST_CODE = 44401;

    public static final String EXTRA_SOURCE = BuildActivity.class.getName() + ".extra_source_file";

    private static final String LOG_TAG = "BuildActivity";
    private static final Pattern REGEX_PACKAGE_NAME = Pattern.compile("^([A-Za-z][A-Za-z\\d_]*\\.)+([A-Za-z][A-Za-z\\d_]*)$");

    private ActivityBuildBinding binding;
    private final List<Option> options = new ArrayList<>();

    private ProjectConfig mProjectConfig;
    private MaterialDialog mProgressDialog;
    private String mSource;
    private boolean mIsDefaultIcon = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuildBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupViews();
    }

    void setupViews() {
        setToolbarAsBack(getString(R.string.text_build_apk));
        preparePermissionView();
        mSource = getIntent().getStringExtra(EXTRA_SOURCE);
        if (mSource != null) {
            setupWithSourceFile(new ScriptFile(mSource));
        }
        checkApkBuilderPlugin();
    }

    /**
     * 构建权限选项列表
     * Powered by ChatGPT3.5
     */
    private void preparePermissionView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Populate the options list
        List<String> permissions = Arrays.asList(
                "android.permission.ACCESS_WIFI_STATE",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.SCHEDULE_EXACT_ALARM",
                "android.permission.QUERY_ALL_PACKAGES",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.MANAGE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.INTERNET",
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
                "android.permission.RECEIVE_BOOT_COMPLETED",
                "android.permission.FOREGROUND_SERVICE",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_PHONE_STATE",
                "com.android.launcher.permission.INSTALL_SHORTCUT",
                "com.android.launcher.permission.UNINSTALL_SHORTCUT"
        );
        Collections.sort(permissions);
        for (String permission : permissions) {
            options.add(new Option(permission, false));
        }
        OptionAdapter adapter = new OptionAdapter(options);
        binding.recyclerView.setAdapter(adapter);
    }

    private void checkApkBuilderPlugin() {
        if (!ApkBuilderPluginHelper.isPluginAvailable(this)) {
            showPluginDownloadDialog(R.string.no_apk_builder_plugin, true);
            return;
        }
        int version = ApkBuilderPluginHelper.getPluginVersion(this);
        if (version < 0) {
            showPluginDownloadDialog(R.string.no_apk_builder_plugin, true);
            return;
        }
        if (version < ApkBuilderPluginHelper.getSuitablePluginVersion()) {
            showPluginDownloadDialog(R.string.apk_builder_plugin_version_too_low, false);
        }
    }

    private void showPluginDownloadDialog(int msgRes, boolean finishIfCanceled) {
        new ThemeColorMaterialDialogBuilder(this)
                .content(msgRes)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> downloadPlugin())
                .onNegative((dialog, which) -> {
                    if (finishIfCanceled) finish();
                })
                .show();

    }

    private void downloadPlugin() {
        IntentUtil.browse(this, String.format(Locale.getDefault(),
                "https://i.autojs.org/autojs/plugin/%d.apk", ApkBuilderPluginHelper.getSuitablePluginVersion()));
    }

    @SuppressLint("StringFormatInvalid")
    private void setupWithSourceFile(ScriptFile file) {
        String dir = file.getParent();
        if (dir.startsWith(getFilesDir().getPath())) {
            dir = Pref.getScriptDirPath();
        }
        binding.outputPath.setText(dir);
        binding.appName.setText(file.getSimplifiedName());
        binding.packageName.setText(getString(R.string.format_default_package_name, System.currentTimeMillis()));
        setSource(file);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    void selectSourceFilePath() {
        String initialDir = new File(binding.sourcePath.getText().toString()).getParent();
        // Android 10+ 使用 StorageUtil 获取默认目录
        String defaultDir = net.cc.cca.tool.StorageUtil.getScriptDirPath(this);
        new FileChooserDialogBuilder(this)
                .title(R.string.text_source_file_path)
                .dir(initialDir == null ? defaultDir : initialDir)
                .singleChoice(this::setSource)
                .show();
    }

    private void setSource(File file) {
        if (!file.isDirectory()) {
            binding.sourcePath.setText(file.getPath());
            return;
        }
        mProjectConfig = ProjectConfig.fromProjectDir(file.getPath());
        if (mProjectConfig == null) {
            return;
        }
        binding.outputPath.setText(new File(mSource, mProjectConfig.getBuildDir()).getPath());
        binding.appConfig.setVisibility(View.GONE);
        binding.sourcePathContainer.setVisibility(View.GONE);
    }

    void selectOutputDirPath() {
        String initialDir = new File(binding.outputPath.getText().toString()).exists() ?
                binding.outputPath.getText().toString() : Pref.getScriptDirPath();
        new FileChooserDialogBuilder(this)
                .title(R.string.text_output_apk_path)
                .dir(initialDir)
                .chooseDir()
                .singleChoice(dir -> binding.outputPath.setText(dir.getPath()))
                .show();
    }

    void selectIcon() {
        Intent intent = new Intent(this, ShortcutIconSelectActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        startActivityForResult(intent, REQUEST_CODE);
    }

    void buildApk() {
        if (!ApkBuilderPluginHelper.isPluginAvailable(this)) {
            Toast.makeText(this, R.string.text_apk_builder_plugin_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkInputs()) {
            return;
        }
        doBuildingApk();
    }

    private boolean checkInputs() {
        boolean inputValid = true;
        inputValid &= checkNotEmpty(binding.sourcePath);
        inputValid &= checkNotEmpty(binding.outputPath);
        inputValid &= checkNotEmpty(binding.appName);
        inputValid &= checkNotEmpty(binding.sourcePath);
        inputValid &= checkNotEmpty(binding.versionCode);
        inputValid &= checkNotEmpty(binding.versionName);
        inputValid &= checkPackageNameValid(binding.packageName);
        return inputValid;
    }

    private boolean checkPackageNameValid(EditText editText) {
        Editable text = editText.getText();
        String hint = ((TextInputLayout) editText.getParent().getParent()).getHint().toString();
        if (TextUtils.isEmpty(text)) {
            editText.setError(hint + getString(R.string.text_should_not_be_empty));
            return false;
        }
        if (!REGEX_PACKAGE_NAME.matcher(text).matches()) {
            editText.setError(getString(R.string.text_invalid_package_name));
            return false;
        }
        return true;

    }

    private boolean checkNotEmpty(EditText editText) {
        if (!TextUtils.isEmpty(editText.getText()) || !editText.isShown())
            return true;
        // TODO: 2017/12/8 more beautiful ways?
        String hint = ((TextInputLayout) editText.getParent().getParent()).getHint().toString();
        editText.setError(hint + getString(R.string.text_should_not_be_empty));
        return false;
    }

    @SuppressLint("CheckResult")
    private void doBuildingApk() {
        ApkBuilder.AppConfig appConfig = createAppConfig();
        File tmpDir = new File(getCacheDir(), "build/");
        File outApk = new File(binding.outputPath.getText().toString(),
                String.format("%s_v%s.apk", appConfig.getAppName(), appConfig.getVersionName()));
        showProgressDialog();
        Observable.fromCallable(() -> callApkBuilder(tmpDir, outApk, appConfig))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(apkBuilder -> onBuildSuccessful(outApk),
                        this::onBuildFailed);
    }

    private ApkBuilder.AppConfig createAppConfig() {
        ApkBuilder.AppConfig appConfig = null;
        if (mProjectConfig != null) {
            appConfig = ApkBuilder.AppConfig.fromProjectConfig(mSource, mProjectConfig);
        } else {
            String jsPath = binding.sourcePath.getText().toString();
            String versionName = binding.versionName.getText().toString();
            int versionCode = Integer.parseInt(binding.versionCode.getText().toString());
            String appName = binding.appName.getText().toString();
            String packageName = binding.packageName.getText().toString();
            appConfig = new ApkBuilder.AppConfig()
                    .setAppName(appName)
                    .setSourcePath(jsPath)
                    .setPackageName(packageName)
                    .setVersionCode(versionCode)
                    .setVersionName(versionName)
                    .setIcon(mIsDefaultIcon ? null : (Callable<Bitmap>) () ->
                            BitmapTool.drawableToBitmap(binding.icon.getDrawable())
                    );
        }
        appConfig.setUseOpenCv(binding.useOpenCv.isChecked());
        appConfig.setUsePaddleOcr(binding.usePaddleOcr.isChecked());
        appConfig.setUseMlKitOcr(binding.useMlKitOcr.isChecked());
        appConfig.setUseOnnx(binding.useOnnxRuntime.isChecked());
        Set<String> enabledPermission = new HashSet<>();
        for (Option option : options) {
            if (option.isSelected()) {
                enabledPermission.add(option.getText());
            }
        }
        appConfig.setEnabledPermission(enabledPermission);
        return appConfig;
    }

    private ApkBuilder callApkBuilder(File tmpDir, File outApk, ApkBuilder.AppConfig appConfig) throws Exception {
        InputStream templateApk = ApkBuilderPluginHelper.openTemplateApk(BuildActivity.this);
        return new ApkBuilder(templateApk, outApk, tmpDir.getPath())
                .setProgressCallback(BuildActivity.this)
                .prepare()
                .withConfig(appConfig)
                .build()
                .sign()
                .cleanWorkspace();
    }

    private void showProgressDialog() {
        mProgressDialog = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .content(R.string.text_on_progress)
                .cancelable(false)
                .show();
    }

    private void onBuildFailed(Throwable error) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        Toast.makeText(this, getString(R.string.text_build_failed) + error.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, "Build failed", error);
    }

    @SuppressLint("StringFormatInvalid")
    private void onBuildSuccessful(File outApk) {
        mProgressDialog.dismiss();
        mProgressDialog = null;
        new MaterialDialog.Builder(this)
                .title(R.string.text_build_successfully)
                .content(getString(R.string.format_build_successfully, outApk.getPath()))
                .positiveText(R.string.text_install)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) ->
                        IntentUtil.installApkOrToast(BuildActivity.this, outApk.getPath(), AppFileProvider.AUTHORITY)
                )
                .show();

    }

    @Override
    public void onPrepare(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_prepare);
    }

    @Override
    public void onBuild(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_build);

    }

    @Override
    public void onSign(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_package);

    }

    @Override
    public void onClean(ApkBuilder builder) {
        mProgressDialog.setContent(R.string.apk_builder_clean);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        ShortcutIconSelectActivity.getBitmapFromIntent(getApplicationContext(), data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    binding.icon.setImageBitmap(bitmap);
                    mIsDefaultIcon = false;
                }, Throwable::printStackTrace);

    }

}
