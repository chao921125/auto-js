package net.cc.cca.ui.main.task;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;

import net.cc.cca.R;
import net.cc.cca.databinding.FragmentTaskManagerBinding;
import net.cc.cca.autojs.AutoJs;
import net.cc.cca.ui.main.ViewPagerFragment;
import net.cc.cca.ui.widget.SimpleAdapterDataObserver;

/**
 * Created by Stardust on 2017/3/24.
 */
public class TaskManagerFragment extends ViewPagerFragment {

    private FragmentTaskManagerBinding binding;
    private TaskListRecyclerView mTaskListRecyclerView;
    private View mNoRunningScriptNotice;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public TaskManagerFragment() {
        super(45);
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public android.view.View onCreateView(android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTaskManagerBinding.inflate(inflater, container, false);
        mTaskListRecyclerView = binding.taskList;
        mNoRunningScriptNotice = binding.noticeNoRunningScript;
        mSwipeRefreshLayout = binding.swipeRefreshLayout;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews();
    }

    void setUpViews() {
        init();
        final boolean noRunningScript = mTaskListRecyclerView.getAdapter().getItemCount() == 0;
        mNoRunningScriptNotice.setVisibility(noRunningScript ? View.VISIBLE : View.GONE);
    }

    private void init() {
        mTaskListRecyclerView.getAdapter().registerAdapterDataObserver(new SimpleAdapterDataObserver() {

            @Override
            public void onSomethingChanged() {
                final boolean noRunningScript = mTaskListRecyclerView.getAdapter().getItemCount() == 0;
                mTaskListRecyclerView.postDelayed(() -> {
                    if (mNoRunningScriptNotice == null)
                        return;
                    mNoRunningScriptNotice.setVisibility(noRunningScript ? View.VISIBLE : View.GONE);
                }, 150);
            }

        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mTaskListRecyclerView.refresh();
            mTaskListRecyclerView.postDelayed(() -> {
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }, 800);
        });
    }

    @Override
    protected void onFabClick(FloatingActionButton fab) {
        AutoJs.getInstance().getScriptEngineService().stopAll();
    }

    @Override
    public boolean onBackPressed(Activity activity) {
        return false;
    }
}
