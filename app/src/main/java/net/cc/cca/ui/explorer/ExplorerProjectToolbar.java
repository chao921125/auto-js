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
import net.cc.cca.databinding.ExplorerProjectToolbarBinding;
import net.cc.cca.autojs.AutoJs;
import net.cc.cca.model.explorer.ExplorerChangeEvent;
import net.cc.cca.model.explorer.ExplorerItem;
import net.cc.cca.model.explorer.Explorers;
import net.cc.cca.ui.project.BuildActivity;
import net.cc.cca.ui.project.ProjectConfigActivity;
import org.greenrobot.eventbus.Subscribe;

public class ExplorerProjectToolbar extends CardView {

    private ProjectConfig mProjectConfig;
    private PFile mDirectory;
    private ExplorerProjectToolbarBinding binding;

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
        binding = ExplorerProjectToolbarBinding.inflate(android.view.LayoutInflater.from(getContext()), this, true);
        binding.run.setOnClickListener(v -> run());
        binding.build.setOnClickListener(v -> build());
        binding.sync.setOnClickListener(v -> sync());
        setOnClickListener(view -> edit());
    }

    public void setProject(PFile dir) {
        mProjectConfig = ProjectConfig.fromProjectDir(dir.getPath());
        if(mProjectConfig == null){
            setVisibility(GONE);
            return;
        }
        mDirectory = dir;
        binding.projectName.setText(mProjectConfig.getName());
    }

    public void refresh() {
        if (mDirectory != null) {
            setProject(mDirectory);
        }
    }

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

    void build() {
        Intent intent = new Intent(getContext(), BuildActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BuildActivity.EXTRA_SOURCE, mDirectory.getPath());
        getContext().startActivity(intent);
    }

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
        Intent intent = new Intent(getContext(), ProjectConfigActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ProjectConfigActivity.EXTRA_DIRECTORY, mDirectory.getPath());
        getContext().startActivity(intent);
    }

}
