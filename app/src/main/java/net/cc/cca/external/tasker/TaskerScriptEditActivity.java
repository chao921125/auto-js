package net.cc.cca.external.tasker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import net.cc.cca.R;
import net.cc.cca.databinding.ActivityTaskerScriptEditBinding;
import net.cc.cca.timing.TaskReceiver;
import net.cc.cca.tool.Observers;
import net.cc.cca.ui.BaseActivity;
import net.cc.cca.ui.edit.EditorView;

import io.reactivex.android.schedulers.AndroidSchedulers;

import static net.cc.cca.ui.edit.EditorView.EXTRA_CONTENT;
import static net.cc.cca.ui.edit.EditorView.EXTRA_NAME;
import static net.cc.cca.ui.edit.EditorView.EXTRA_RUN_ENABLED;
import static net.cc.cca.ui.edit.EditorView.EXTRA_SAVE_ENABLED;

/**
 * Created by Stardust on 2017/4/5.
 */
public class TaskerScriptEditActivity extends BaseActivity {

    public static final int REQUEST_CODE = 10016;
    public static final String EXTRA_TASK_ID = TaskReceiver.EXTRA_TASK_ID;

    public static void edit(Activity activity, String title, String summary, String content) {
        activity.startActivityForResult(new Intent(activity, TaskerScriptEditActivity.class)
                .putExtra(EXTRA_CONTENT, content)
                .putExtra("summary", summary)
                .putExtra(EXTRA_NAME, title), REQUEST_CODE);
    }

    private ActivityTaskerScriptEditBinding binding;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskerScriptEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpViews();
    }

    @SuppressLint("CheckResult")
    void setUpViews() {
        binding.editorView.handleIntent(getIntent()
                .putExtra(EXTRA_RUN_ENABLED, false)
                .putExtra(EXTRA_SAVE_ENABLED, false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Observers.emptyConsumer(),
                        ex -> {
                            if (ex.getMessage() != null) {
                                Toast.makeText(TaskerScriptEditActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            finish();
                        });
        BaseActivity.setToolbarAsBack(this, R.id.toolbar, binding.editorView.getName());
    }


    @Override
    public void finish() {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_CONTENT, binding.editorView.getEditor().getText()));
        TaskerScriptEditActivity.super.finish();
    }

    @Override
    protected void onDestroy() {
        binding.editorView.destroy();
        super.onDestroy();
    }
}
