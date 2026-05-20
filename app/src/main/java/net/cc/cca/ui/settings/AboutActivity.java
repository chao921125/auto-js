package net.cc.cca.ui.settings;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import net.cc.cca.autojs.AutoJs;
import net.cc.cca.databinding.ActivityAboutBinding;
import net.cc.cca.timing.TimedTaskManager;
import net.cc.cca.tool.IntentTool;
import net.cc.cca.ui.BaseActivity;
import net.cc.cca.theme.dialog.ThemeColorMaterialDialogBuilder;

import com.stardust.util.IntentUtil;
import com.tencent.bugly.crashreport.CrashReport;

import net.cc.cca.BuildConfig;
import net.cc.cca.R;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

/**
 * Created by Stardust on 2017/2/2.
 */
public class AboutActivity extends BaseActivity {

    private static final String TAG = "AboutActivity";
    private ActivityAboutBinding binding;

    private int mLolClickCount = 0;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setUpViews();
        setupClickListeners();
    }

    void setUpViews() {
        setVersionName();
        setToolbarAsBack(getString(R.string.text_about));
    }

    private void setupClickListeners() {
        binding.github.setOnClickListener(v -> openGitHub());
        binding.newGithub.setOnClickListener(v -> openModifiedGitHub());
        binding.qq.setOnClickListener(v -> openQQToChatWithMe());
        binding.iconContainer.setOnClickListener(v -> showDebugInfo());
        binding.email.setOnClickListener(v -> openEmailToSendMe());
        binding.icon.setOnClickListener(v -> lol());
        binding.developer.setOnClickListener(v -> hhh());
        binding.modifier.setOnClickListener(v -> hhhh());
    }

    @SuppressLint("SetTextI18n")
    private void setVersionName() {
        binding.version.setText("Version " + BuildConfig.VERSION_NAME);
    }

    void openGitHub() {
        IntentTool.browse(this, getString(R.string.my_github));
    }

    void openModifiedGitHub() {
        IntentTool.browse(this, getString(R.string.new_github_repo));
    }

    void openQQToChatWithMe() {
        String qq = getString(R.string.qq);
        if (!IntentUtil.chatWithQQ(this, qq)) {
            Toast.makeText(this, R.string.text_mobile_qq_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("CheckResult")
    void showDebugInfo() {
        final long currentMillis = System.currentTimeMillis();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final String currentDateTimeStr = LocalDateTime.now().format(formatter);
        TimedTaskManager.getInstance().getAllTasks().forEach(timedTask -> {
            if (timedTask.getNextTime() < currentMillis) {
                AutoJs.getInstance().debugInfo("timedTask is out date" +
                        " nextTime:" + LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timedTask.getNextTime()), ZoneId.of("GMT+8"))
                        .format(formatter) + " millis: " + timedTask.getMillis()
                        + " current: " + currentDateTimeStr + " info:" + timedTask);
            } else {
                AutoJs.getInstance().debugInfo("timedTask is not ready" +
                        " nextTime:" + LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timedTask.getNextTime()), ZoneId.of("GMT+8"))
                        .format(formatter) + " millis: " + timedTask.getMillis()
                        + " current: " + currentDateTimeStr + " info:" + timedTask);
            }
        });
    }

    void openEmailToSendMe() {
        String email = getString(R.string.email);
        IntentUtil.sendMailTo(this, email);
    }


//    void share() {
//        IntentUtil.shareText(this, getString(R.string.share_app));
//    }

    void lol() {
        mLolClickCount++;
        //Toast.makeText(this, R.string.text_lll, Toast.LENGTH_LONG).show();
        if (mLolClickCount >= 5) {
            crashTest();
            //showEasterEgg();
        }
    }

    private void showEasterEgg() {
        new MaterialDialog.Builder(this)
                .customView(R.layout.paint_layout, false)
                .show();
    }

    private void crashTest() {
        new ThemeColorMaterialDialogBuilder(this)
                .title("Crash Test")
                .positiveText("Crash")
                .onPositive((dialog, which) -> {
                    CrashReport.testJavaCrash();
                }).show();
    }

    void hhh() {
        Toast.makeText(this, R.string.text_it_is_the_developer_of_app, Toast.LENGTH_LONG).show();
    }

    void hhhh() {
        Toast.makeText(this, R.string.text_it_is_the_modifier_of_app, Toast.LENGTH_LONG).show();
    }


}
