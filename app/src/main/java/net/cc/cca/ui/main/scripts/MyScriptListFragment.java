package net.cc.cca.ui.main.scripts;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.stardust.app.GlobalAppContext;
import com.stardust.util.IntentUtil;

import net.cc.cca.Pref;
import net.cc.cca.R;
import net.cc.cca.databinding.FragmentMyScriptListBinding;
import net.cc.cca.external.fileprovider.AppFileProvider;
import net.cc.cca.model.explorer.ExplorerDirPage;
import net.cc.cca.model.explorer.Explorers;
import net.cc.cca.model.script.ScriptFile;
import net.cc.cca.model.script.Scripts;
import net.cc.cca.theme.dialog.ThemeColorMaterialDialogBuilder;
import net.cc.cca.tool.SimpleObserver;
import net.cc.cca.ui.common.ScriptOperations;
import net.cc.cca.ui.edit.EditorView;
import net.cc.cca.ui.explorer.ExplorerView;
import net.cc.cca.ui.main.FloatingActionMenu;
import net.cc.cca.ui.main.QueryEvent;
import net.cc.cca.ui.main.ViewPagerFragment;
import net.cc.cca.ui.project.ProjectConfigActivity;
import net.cc.cca.ui.viewmodel.ExplorerItemList;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Stardust on 2017/3/13.
 */
public class MyScriptListFragment extends ViewPagerFragment implements FloatingActionMenu.OnFloatingActionButtonClickListener {

    private static final String TAG = "MyScriptListFragment";

    private FragmentMyScriptListBinding binding;
    private ExplorerView mExplorerView;
    private FloatingActionMenu mFloatingActionMenu;

    public MyScriptListFragment() {
        super(0);
    }

    @Nullable
    @Override
    public android.view.View onCreateView(android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyScriptListBinding.inflate(inflater, container, false);
        mExplorerView = binding.scriptFileList;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(android.view.View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    void setUpViews() {
        ExplorerItemList.SortConfig sortConfig = ExplorerItemList.SortConfig.from(PreferenceManager.getDefaultSharedPreferences(getContext()));
        mExplorerView.setSortConfig(sortConfig);
        mExplorerView.setExplorer(Explorers.workspace(), ExplorerDirPage.createRoot(Pref.getScriptDirPath()));
        mExplorerView.setOnItemClickListener((view, item) -> {
            if (item.isEditable()) {
                ScriptFile scriptFile = item.toScriptFile();
                Log.d(TAG, "setUpViews: selected file size: " + scriptFile.length());
                if (scriptFile.length() > EditorView.MAX_EDITABLE_SIZE) {
                    new ThemeColorMaterialDialogBuilder(getContext())
                            .title(getString(R.string.text_cannot_read_file))
                            .content("当前文件过大，直接编辑可能导致卡死")
                            .positiveText(R.string.text_cancel)
                            .negativeText("使用其他方式打开")
                            .cancelable(false)
                            .onPositive((dialog, which) -> {})
                            .onNegative(((dialog, which) -> {
                                IntentUtil.viewFile(GlobalAppContext.get(), item.getPath(), AppFileProvider.AUTHORITY);
                            }))
                            .show();
                    return;
                }
                Scripts.INSTANCE.edit(getActivity(), item.toScriptFile());
            } else {
                IntentUtil.viewFile(GlobalAppContext.get(), item.getPath(), AppFileProvider.AUTHORITY);
            }
        });
    }

    @Override
    protected void onFabClick(FloatingActionButton fab) {
        initFloatingActionMenuIfNeeded(fab);
        if (mFloatingActionMenu.isExpanded()) {
            mFloatingActionMenu.collapse();
        } else {
            mFloatingActionMenu.expand();

        }
    }

    private void initFloatingActionMenuIfNeeded(final FloatingActionButton fab) {
        if (mFloatingActionMenu != null)
            return;
        mFloatingActionMenu = getActivity().findViewById(R.id.floating_action_menu);
        mFloatingActionMenu.getState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<Boolean>() {
                    @Override
                    public void onNext(@io.reactivex.annotations.NonNull Boolean expanding) {
                        fab.animate()
                                .rotation(expanding ? 45 : 0)
                                .setDuration(300)
                                .start();
                    }
                });
        mFloatingActionMenu.setOnFloatingActionButtonClickListener(this);
    }

    @Override
    public boolean onBackPressed(Activity activity) {
        if (mFloatingActionMenu != null && mFloatingActionMenu.isExpanded()) {
            mFloatingActionMenu.collapse();
            return true;
        }
        if (mExplorerView.canGoBack()) {
            mExplorerView.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void onPageHide() {
        super.onPageHide();
        if (mFloatingActionMenu != null && mFloatingActionMenu.isExpanded()) {
            mFloatingActionMenu.collapse();
        }
    }

    @Subscribe
    public void onQuerySummit(QueryEvent event) {
        if (!isShown()) {
            return;
        }
        if (event == QueryEvent.CLEAR) {
            mExplorerView.setFilter(null);
            return;
        }
        String query = event.getQuery();
        mExplorerView.setFilter((item -> item.getName().contains(query)));
    }

    @Override
    public void onStop() {
        super.onStop();
        mExplorerView.getSortConfig().saveInto(PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mFloatingActionMenu != null)
            mFloatingActionMenu.setOnFloatingActionButtonClickListener(null);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(FloatingActionButton button, int pos) {
        if (mExplorerView == null)
            return;
        switch (pos) {
            case 0:
                new ScriptOperations(getContext(), mExplorerView, mExplorerView.getCurrentPage())
                        .newDirectory();
                break;
            case 1:
                new ScriptOperations(getContext(), mExplorerView, mExplorerView.getCurrentPage())
                        .newFile();
                break;
            case 2:
                new ScriptOperations(getContext(), mExplorerView, mExplorerView.getCurrentPage())
                        .importFile();
                break;
            case 3:
                ProjectConfigActivity.builder(getContext())
                        .putExtra(ProjectConfigActivity.EXTRA_PARENT_DIRECTORY, mExplorerView.getCurrentPage().getPath())
                        .putExtra(ProjectConfigActivity.EXTRA_NEW_PROJECT, true)
                        .startActivity();
                break;

        }
    }
}
