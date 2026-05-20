package net.cc.cca.ui.explorer;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.Toast;

import net.cc.stardust.project.ProjectConfig;
import net.cc.stardust.project.ProjectLauncher;
import com.stardust.pio.PFile;

import net.cc.cca.R;
import net.cc.cca.autojs.AutoJs;
import net.cc.cca.model.explorer.ExplorerChangeEvent;
import net.cc.cca.model.explorer.ExplorerItem;
import net.cc.cca.model.explorer.Explorers;
import net.cc.cca.ui.project.BuildActivity;
import net.cc.cca.ui.project.BuildActivity_;
import net.cc.cca.ui.project.ProjectConfigActivity;
import net.cc.cca.ui.project.ProjectConfigActivity_;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExplorerProjectToolbar extends CardView {

    private ProjectConfig mProjectConfig;
    private PFile mDirectory;

    @BindView(R.id.project_name)
    TextView mProjectName;

    public ExplorerProjectToolbar(Context context) {
        super(context);
        init();
    }

    public ExplorerProjectToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExplorerProjectToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.explorer_project_toolbar, this);
        ButterKnife.bind(this);
        setOnClickListener(view -> edit());
    }

    public void setProject(PFile dir) {
        mProjectConfig = ProjectConfig.fromProjectDir(dir.getPath());
        if(mProjectConfig == null){
            setVisibility(GONE);
            return;
        }
        mDirectory = dir;
        mProjectName.setText(mProjectConfig.getName());
    }

    public void refresh() {
        if (mDirectory != null) {
            setProject(mDirectory);
        }
    }

    @OnClick(R.id.run)
    void run() {
        try {
            new ProjectLauncher(mDirectory.getPath())
                    .launch(AutoJs.getInstance().getScriptEngineService());
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.build)
    void build() {
        BuildActivity_.intent(getContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .extra(BuildActivity.EXTRA_SOURCE, mDirectory.getPath())
                .start();
    }

    @OnClick(R.id.sync)
    void sync() {

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Explorers.workspace().registerChangeListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Explorers.workspace().unregisterChangeListener(this);
    }

    @Subscribe
    public void onExplorerChange(ExplorerChangeEvent event) {
        if (mDirectory == null) {
            return;
        }
        ExplorerItem item = event.getItem();
        if ((event.getAction() == ExplorerChangeEvent.ALL)
                || (item != null && mDirectory.getPath().equals(item.getPath()))) {
            refresh();
        }
    }

    void edit() {
        ProjectConfigActivity_.intent(getContext())
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .extra(ProjectConfigActivity.EXTRA_DIRECTORY, mDirectory.getPath())
                .start();
    }

}
