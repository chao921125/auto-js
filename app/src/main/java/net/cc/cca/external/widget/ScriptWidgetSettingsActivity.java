package net.cc.cca.external.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import net.cc.cca.R;
import net.cc.cca.databinding.ActivityScriptWidgetSettingsBinding;
import net.cc.cca.model.explorer.Explorer;
import net.cc.cca.model.explorer.ExplorerDirPage;
import net.cc.cca.model.explorer.ExplorerFileProvider;
import net.cc.cca.model.script.Scripts;
import net.cc.cca.ui.BaseActivity;
import net.cc.cca.ui.explorer.ExplorerView;

/**
 * Created by Stardust on 2017/7/11.
 */
public class ScriptWidgetSettingsActivity extends BaseActivity {

    private String mSelectedScriptFilePath;
    private Explorer mExplorer;
    private int mAppWidgetId;
    private ActivityScriptWidgetSettingsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScriptWidgetSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        setUpViews();
    }

    void setUpViews() {
        BaseActivity.setToolbarAsBack(this, R.id.toolbar, getString(R.string.text_please_choose_a_script));
        initScriptListRecyclerView();
    }


    private void initScriptListRecyclerView() {
        mExplorer = new Explorer(new ExplorerFileProvider(Scripts.INSTANCE.getFILE_FILTER()), 0);
        ExplorerView explorerView = findViewById(R.id.script_list);
        // Android 10+ 使用 StorageUtil 获取脚本目录
        String scriptDir = net.cc.cca.tool.StorageUtil.getScriptDirPath(this);
        explorerView.setExplorer(mExplorer, ExplorerDirPage.createRoot(new File(scriptDir)));
        explorerView.setOnItemClickListener((view, file) -> {
            mSelectedScriptFilePath = file.getPath();
            finish();
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mExplorer.refreshAll();
        } else if (item.getItemId() == R.id.action_clear_file_selection) {
            mSelectedScriptFilePath = null;
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.script_widget_settings_menu, menu);
        return true;
    }

    @Override
    public void finish() {
        if (ScriptWidget.updateWidget(this, mAppWidgetId, mSelectedScriptFilePath)) {
            setResult(RESULT_OK, new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));

        } else {
            setResult(RESULT_CANCELED, new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
        }
        super.finish();
    }


}
