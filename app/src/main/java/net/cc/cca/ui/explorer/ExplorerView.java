package net.cc.cca.ui.explorer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.stardust.pio.PFiles;

import net.cc.cca.R;
import net.cc.cca.databinding.ScriptFileListFileBinding;
import net.cc.cca.databinding.ScriptFileListDirectoryBinding;
import net.cc.cca.databinding.ScriptFileListCategoryBinding;
import net.cc.cca.model.explorer.Explorer;
import net.cc.cca.model.explorer.ExplorerChangeEvent;
import net.cc.cca.model.explorer.ExplorerDirPage;
import net.cc.cca.model.explorer.ExplorerFileItem;
import net.cc.cca.model.explorer.ExplorerItem;
import net.cc.cca.model.explorer.ExplorerPage;
import net.cc.cca.model.explorer.ExplorerProjectPage;
import net.cc.cca.model.explorer.ExplorerSampleItem;
import net.cc.cca.model.explorer.ExplorerSamplePage;
import net.cc.cca.model.explorer.Explorers;
import net.cc.cca.model.script.ScriptFile;
import net.cc.cca.model.script.Scripts;
import net.cc.cca.tool.Observers;
import net.cc.cca.ui.project.BuildActivity;
import net.cc.cca.ui.project.BuildActivity;
import net.cc.cca.ui.common.ScriptLoopDialog;
import net.cc.cca.ui.common.ScriptOperations;
import net.cc.cca.ui.viewmodel.ExplorerItemList;
import net.cc.cca.ui.widget.BindableViewHolder;
import net.cc.cca.theme.widget.ThemeColorSwipeRefreshLayout;

import net.cc.cca.workground.WrapContentGridLayoutManger;
import org.greenrobot.eventbus.Subscribe;

import java.util.Stack;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stardust on 2017/8/21.
 */

public class ExplorerView extends ThemeColorSwipeRefreshLayout implements SwipeRefreshLayout.OnRefreshListener, PopupMenu.OnMenuItemClickListener {

    private static final String LOG_TAG = "ExplorerView";

    public interface OnItemClickListener {
        void onItemClick(View view, ExplorerItem item);
    }

    public interface OnItemOperatedListener {
        void OnItemOperated(ExplorerItem item);
    }

    protected static final int VIEW_TYPE_ITEM = 0;
    protected static final int VIEW_TYPE_PAGE = 1;
    //category是类别，也即"文件", "文件夹"那两个
    protected static final int VIEW_TYPE_CATEGORY = 2;

    private static final int positionOfCategoryDir = 0;

    private ExplorerItemList mExplorerItemList = new ExplorerItemList();
    private RecyclerView mExplorerItemListView;
    private ExplorerProjectToolbar mProjectToolbar;
    private ExplorerAdapter mExplorerAdapter = new ExplorerAdapter();
    protected OnItemClickListener mOnItemClickListener;
    private Function<ExplorerItem, Boolean> mFilter;
    private OnItemOperatedListener mOnItemOperatedListener;
    protected ExplorerItem mSelectedItem;
    private Explorer mExplorer;
    private Stack<ExplorerPageState> mPageStateHistory = new Stack<>();
    private ExplorerPageState mCurrentPageState = new ExplorerPageState();
    private boolean mDirSortMenuShowing = false;
    private int mDirectorySpanSize = 2;

    public ExplorerView(Context context) {
        super(context);
        init();
    }

    public ExplorerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExplorerPage getCurrentPage() {
        return mCurrentPageState.page;
    }

    public void setRootPage(ExplorerPage page) {
        mPageStateHistory.clear();
        setCurrentPageState(new ExplorerPageState(page));
        loadItemList();
    }

    private void setCurrentPageState(ExplorerPageState currentPageState) {
        mCurrentPageState = currentPageState;
        if (mCurrentPageState.page instanceof ExplorerProjectPage) {
            mProjectToolbar.setVisibility(VISIBLE);
            mProjectToolbar.setProject(currentPageState.page.toScriptFile());
        } else {
            mProjectToolbar.setVisibility(GONE);
        }
    }

    protected void enterDirectChildPage(ExplorerPage childItemGroup) {
        mCurrentPageState.scrollY = ((LinearLayoutManager) mExplorerItemListView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        mPageStateHistory.push(mCurrentPageState);
        setCurrentPageState(new ExplorerPageState(childItemGroup));
        loadItemList();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setSortConfig(ExplorerItemList.SortConfig sortConfig) {
        mExplorerItemList.setSortConfig(sortConfig);
    }

    public ExplorerItemList.SortConfig getSortConfig() {
        return mExplorerItemList.getSortConfig();
    }

    public void setExplorer(Explorer explorer, ExplorerPage rootPage) {
        if (mExplorer != null)
            mExplorer.unregisterChangeListener(this);
        mExplorer = explorer;
        setRootPage(rootPage);
        mExplorer.registerChangeListener(this);
    }

    public void setExplorer(Explorer explorer, ExplorerPage rootPage, ExplorerPage currentPage) {
        if (mExplorer != null)
            mExplorer.unregisterChangeListener(this);
        mExplorer = explorer;
        mPageStateHistory.clear();
        setCurrentPageState(new ExplorerPageState(rootPage));
        mExplorer.registerChangeListener(this);
        enterChildPage(currentPage);
    }

    public void enterChildPage(ExplorerPage childPage) {
        ScriptFile root = mCurrentPageState.page.toScriptFile();
        ScriptFile dir = childPage.toScriptFile();
        Stack<ScriptFile> dirs = new Stack<>();
        while (!dir.equals(root)) {
            dir = dir.getParentFile();
            if (dir == null) {
                break;
            }
            dirs.push(dir);
        }
        ExplorerDirPage parent = null;
        while (!dirs.empty()) {
            dir = dirs.pop();
            ExplorerDirPage dirPage = new ExplorerDirPage(dir, parent);
            mPageStateHistory.push(new ExplorerPageState(dirPage));
            parent = dirPage;
        }
        setCurrentPageState(new ExplorerPageState(childPage));
        loadItemList();
    }

    public void setOnItemOperatedListener(OnItemOperatedListener onItemOperatedListener) {
        mOnItemOperatedListener = onItemOperatedListener;
    }

    public boolean canGoBack() {
        return !mPageStateHistory.empty();
    }

    public void goBack() {
        setCurrentPageState(mPageStateHistory.pop());
        loadItemList();
    }

    public void setDirectorySpanSize(int directorySpanSize) {
        mDirectorySpanSize = directorySpanSize;
    }

    public void setFilter(Function<ExplorerItem, Boolean> filter) {
        mFilter = filter;
        reload();
    }

    public void reload() {
        loadItemList();
    }

    private void init() {
        Log.d(LOG_TAG, "item bg = " + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.item_background)));
        setOnRefreshListener(this);
        inflate(getContext(), R.layout.explorer_view, this);
        mExplorerItemListView = findViewById(R.id.explorer_item_list);
        mProjectToolbar = findViewById(R.id.project_toolbar);
        initExplorerItemListView();
    }

    private void initExplorerItemListView() {
        mExplorerItemListView.setAdapter(mExplorerAdapter);
        WrapContentGridLayoutManger manager = new WrapContentGridLayoutManger(getContext(), 2);
        manager.setDebugInfo("ExplorerView");
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //For directories
                if (position > positionOfCategoryDir && position < positionOfCategoryFile()) {
                    return mDirectorySpanSize;
                }
                //For files and category
                return 2;
            }
        });
        mExplorerItemListView.setLayoutManager(manager);
    }

    private int positionOfCategoryFile() {
        if (mCurrentPageState.dirsCollapsed)
            return 1;
        return mExplorerItemList.groupCount() + 1;
    }

    @SuppressLint("CheckResult")
    private void loadItemList() {
        setRefreshing(true);
        mExplorer.fetchChildren(mCurrentPageState.page)
                .subscribeOn(Schedulers.io())
                .flatMapObservable(page -> {
                    mCurrentPageState.page = page;
                    return Observable.fromIterable(page);
                })
                .filter(f -> mFilter == null ? true : mFilter.apply(f))
                .collectInto(mExplorerItemList.cloneConfig(), ExplorerItemList::add)
                .observeOn(Schedulers.computation())
                .doOnSuccess(ExplorerItemList::sort)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mExplorerItemList = list;
                    mExplorerAdapter.notifyDataSetChanged();
                    setRefreshing(false);
                    post(() ->
                            mExplorerItemListView.scrollToPosition(mCurrentPageState.scrollY)
                    );
                });
    }

    @Subscribe
    public void onExplorerChange(ExplorerChangeEvent event) {
        Log.d(LOG_TAG, "on explorer change: " + event);
        if ((event.getAction() == ExplorerChangeEvent.ALL)) {
            loadItemList();
            return;
        }
        String currentDirPath = mCurrentPageState.page.getPath();
        String changedDirPath = event.getPage().getPath();
        ExplorerItem item = event.getItem();
        String changedItemPath = item == null ? null : item.getPath();
        if (currentDirPath.equals(changedItemPath) || (currentDirPath.equals(changedDirPath) &&
                event.getAction() == ExplorerChangeEvent.CHILDREN_CHANGE)) {
            loadItemList();
            return;
        }
        if (currentDirPath.equals(changedDirPath)) {
            int i;
            switch (event.getAction()) {
                case ExplorerChangeEvent.CHANGE:
                    i = mExplorerItemList.update(item, event.getNewItem());
                    if (i >= 0) {
                        mExplorerAdapter.notifyItemChanged(item, i);
                    }
                    break;
                case ExplorerChangeEvent.CREATE:
                    mExplorerItemList.insertAtFront(event.getNewItem());
                    mExplorerAdapter.notifyItemInserted(event.getNewItem(), 0);
                    break;
                case ExplorerChangeEvent.REMOVE:
                    i = mExplorerItemList.remove(item);
                    if (i >= 0) {
                        mExplorerAdapter.notifyItemRemoved(item, i);
                    }
                    break;
            }
        }
    }

    @Override
    public void onRefresh() {
        mExplorer.notifyChildrenChanged(mCurrentPageState.page);
        mProjectToolbar.refresh();
    }


    public ScriptFile getCurrentDirectory() {
        return getCurrentPage().toScriptFile();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.rename) {
            new ScriptOperations(getContext(), this, getCurrentPage())
                    .rename((ExplorerFileItem) mSelectedItem)
                    .subscribe(Observers.emptyObserver());
        } else if (itemId == R.id.delete) {
            new ScriptOperations(getContext(), this, getCurrentPage())
                    .delete(mSelectedItem.toScriptFile());
        } else if (itemId == R.id.run_repeatedly) {
            new ScriptLoopDialog(getContext(), mSelectedItem.toScriptFile())
                    .show();
            notifyOperated();
        } else if (itemId == R.id.create_shortcut) {
            new ScriptOperations(getContext(), this, getCurrentPage())
                    .createShortcut(mSelectedItem.toScriptFile());
        } else if (itemId == R.id.open_by_other_apps) {
            Scripts.INSTANCE.openByOtherApps(mSelectedItem.toScriptFile());
            notifyOperated();
        } else if (itemId == R.id.send) {
            Scripts.INSTANCE.send(mSelectedItem.toScriptFile());
            notifyOperated();
        } else if (itemId == R.id.timed_task) {
            new ScriptOperations(getContext(), this, getCurrentPage())
                    .timedTask(mSelectedItem.toScriptFile());
            notifyOperated();
        } else if (itemId == R.id.action_build_apk) {
            Intent intent = new Intent(getContext(), BuildActivity.class);
            intent.putExtra(BuildActivity.EXTRA_SOURCE, mSelectedItem.getPath());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            notifyOperated();
        } else if (itemId == R.id.action_sort_by_date) {
            sort(ExplorerItemList.SORT_TYPE_DATE, mDirSortMenuShowing);
        } else if (itemId == R.id.action_sort_by_type) {
            sort(ExplorerItemList.SORT_TYPE_TYPE, mDirSortMenuShowing);
        } else if (itemId == R.id.action_sort_by_name) {
            sort(ExplorerItemList.SORT_TYPE_NAME, mDirSortMenuShowing);
        } else if (itemId == R.id.action_sort_by_size) {
            sort(ExplorerItemList.SORT_TYPE_SIZE, mDirSortMenuShowing);
        } else if (itemId == R.id.reset) {
            Explorers.Providers.workspace().resetSample(mSelectedItem.toScriptFile())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ignored -> {
                        Snackbar.make(this, R.string.text_reset_succeed, Snackbar.LENGTH_SHORT).show();
                    }, Observers.toastMessage());
        } else {
            return false;
        }
        return true;
    }

    protected void notifyOperated() {
        if (mOnItemOperatedListener != null) {
            mOnItemOperatedListener.OnItemOperated(mSelectedItem);
        }
    }

    @SuppressLint("CheckResult")
    private void sort(final int sortType, final boolean isDir) {
        setRefreshing(true);
        Observable.fromCallable(() -> {
            if (isDir) {
                mExplorerItemList.sortItemGroup(sortType);
            } else {
                mExplorerItemList.sortFile(sortType);
            }
            return mExplorerItemList;
        })

                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    mExplorerAdapter.notifyDataSetChanged();
                    setRefreshing(false);
                });
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mExplorer != null)
            mExplorer.registerChangeListener(this);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mExplorer.unregisterChangeListener(this);
    }


    protected BindableViewHolder<?> onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            return new ExplorerItemViewHolder(inflater.inflate(R.layout.script_file_list_file, parent, false));
        } else if (viewType == VIEW_TYPE_PAGE) {
            return new ExplorerPageViewHolder(inflater.inflate(R.layout.script_file_list_directory, parent, false));
        } else {
            return new CategoryViewHolder(inflater.inflate(R.layout.script_file_list_category, parent, false));
        }
    }

    protected RecyclerView getExplorerItemListView() {
        return mExplorerItemListView;
    }

    private class ExplorerAdapter extends RecyclerView.Adapter<BindableViewHolder<?>> {

        @Override
        public BindableViewHolder<?> onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            return ExplorerView.this.onCreateViewHolder(inflater, parent, viewType);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onBindViewHolder(BindableViewHolder<?> holder, int position) {
            int positionOfCategoryFile = positionOfCategoryFile();
            BindableViewHolder bindableViewHolder = (BindableViewHolder) holder;
            if (position == positionOfCategoryDir || position == positionOfCategoryFile) {
                bindableViewHolder.bind(position == positionOfCategoryDir, position);
                return;
            }
            if (position < positionOfCategoryFile) {
                bindableViewHolder.bind(mExplorerItemList.getItemGroup(position - 1), position);
                return;
            }
            bindableViewHolder.bind(mExplorerItemList.getItem(position - positionOfCategoryFile - 1), position);
        }

        @Override
        public int getItemViewType(int position) {
            int positionOfCategoryFile = positionOfCategoryFile();
            if (position == positionOfCategoryDir || position == positionOfCategoryFile) {
                return VIEW_TYPE_CATEGORY;
            } else if (position < positionOfCategoryFile) {
                return VIEW_TYPE_PAGE;
            } else {
                return VIEW_TYPE_ITEM;
            }
        }

        int getItemPosition(ExplorerItem item, int i) {
            if (item instanceof ExplorerPage) {
                return i + positionOfCategoryDir + 1;
            }
            return i + positionOfCategoryFile() + 1;
        }

        public void notifyItemChanged(ExplorerItem item, int i) {
            notifyItemChanged(getItemPosition(item, i));
        }

        public void notifyItemRemoved(ExplorerItem item, int i) {
            notifyItemRemoved(getItemPosition(item, i));
        }

        public void notifyItemInserted(ExplorerItem item, int i) {
            notifyItemInserted(getItemPosition(item, i));
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (!mCurrentPageState.dirsCollapsed) {
                count += mExplorerItemList.groupCount();
            }
            if (!mCurrentPageState.filesCollapsed) {
                count += mExplorerItemList.itemCount();
            }
            return count + 2;
        }
    }

    protected class ExplorerItemViewHolder extends BindableViewHolder<ExplorerItem> {

        TextView mName;
        TextView mFirstChar;
        TextView mDesc;
        View mOptions;
        View mEdit;
        View mRun;
        GradientDrawable mFirstCharBackground;
        private ExplorerItem mExplorerItem;
        private ScriptFileListFileBinding binding;

        ExplorerItemViewHolder(View itemView) {
            super(itemView);
            binding = ScriptFileListFileBinding.bind(itemView);
            mName = binding.name;
            mFirstChar = binding.firstChar;
            mDesc = binding.desc;
            mOptions = binding.more;
            mEdit = binding.edit;
            mRun = binding.run;
            mFirstCharBackground = (GradientDrawable) mFirstChar.getBackground();
            
            itemView.setOnClickListener(v -> onItemClick());
            mRun.setOnClickListener(v -> run());
            mEdit.setOnClickListener(v -> edit());
            mOptions.setOnClickListener(v -> showOptionMenu());
        }

        @Override
        public void bind(ExplorerItem item, int position) {
            mExplorerItem = item;
            mName.setText(ExplorerViewHelper.getDisplayName(item));
            mDesc.setText(PFiles.getHumanReadableSize(item.getSize()));
            mFirstChar.setText(ExplorerViewHelper.getIconText(item));
            mFirstCharBackground.setColor(ExplorerViewHelper.getIconColor(item));
            mEdit.setVisibility(item.isEditable() ? VISIBLE : GONE);
            mRun.setVisibility(item.isExecutable() ? VISIBLE : GONE);
        }

        void onItemClick() {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(itemView, mExplorerItem);
            }
            notifyOperated();
        }

        void run() {
            Scripts.INSTANCE.run(new ScriptFile(mExplorerItem.getPath()));
            notifyOperated();
        }

        void edit() {
            Scripts.INSTANCE.edit(getContext(), new ScriptFile(mExplorerItem.getPath()));
            notifyOperated();
        }

        void showOptionMenu() {
            mSelectedItem = mExplorerItem;
            PopupMenu popupMenu = new PopupMenu(getContext(), mOptions);
            popupMenu.inflate(R.menu.menu_script_options);
            Menu menu = popupMenu.getMenu();
            if (!mExplorerItem.isExecutable()) {
                menu.removeItem(R.id.run_repeatedly);
                menu.removeItem(R.id.more);
            }
            if (!mExplorerItem.canDelete()) {
                menu.removeItem(R.id.delete);
            }
            if (!mExplorerItem.canRename()) {
                menu.removeItem(R.id.rename);
            }
            if (!(mExplorerItem instanceof ExplorerSampleItem)) {
                menu.removeItem(R.id.reset);
            }
            popupMenu.setOnMenuItemClickListener(ExplorerView.this);
            popupMenu.show();
        }
    }

    protected class ExplorerPageViewHolder extends BindableViewHolder<ExplorerPage> {

        public TextView mName;
        public View mOptions;
        public ImageView mIcon;
        private ExplorerPage mExplorerPage;
        private ScriptFileListDirectoryBinding binding;

        ExplorerPageViewHolder(View itemView) {
            super(itemView);
            binding = ScriptFileListDirectoryBinding.bind(itemView);
            mName = binding.name;
            mOptions = binding.more;
            mIcon = binding.icon;
        }

        @Override
        public void bind(ExplorerPage data, int position) {
            mName.setText(ExplorerViewHelper.getDisplayName(data));
            mIcon.setImageResource(ExplorerViewHelper.getIcon(data));
            mOptions.setVisibility(data instanceof ExplorerSamplePage ? GONE : VISIBLE);
            mExplorerPage = data;

        }

        void onItemClick() {
            enterDirectChildPage(mExplorerPage);
        }

        void showOptionMenu() {
            mSelectedItem = mExplorerPage;
            PopupMenu popupMenu = new PopupMenu(getContext(), mOptions);
            popupMenu.inflate(R.menu.menu_dir_options);
            popupMenu.setOnMenuItemClickListener(ExplorerView.this);
            popupMenu.show();
        }
    }

    class CategoryViewHolder extends BindableViewHolder<Boolean> {

        TextView mTitle;
        ImageView mSort;
        ImageView mSortOrder;
        ImageView mGoBack;
        ImageView mArrow;
        private boolean mIsDir;
        private ScriptFileListCategoryBinding binding;

        CategoryViewHolder(View itemView) {
            super(itemView);
            binding = ScriptFileListCategoryBinding.bind(itemView);
            mTitle = binding.title;
            mSort = binding.sort;
            mSortOrder = binding.order;
            mGoBack = binding.back;
            mArrow = binding.collapse;
            
            mSortOrder.setOnClickListener(v -> changeSortOrder());
            mSort.setOnClickListener(v -> showSortOptions());
            mGoBack.setOnClickListener(v -> back());
            binding.titleContainer.setOnClickListener(v -> collapseOrExpand());
        }

        @Override
        public void bind(Boolean isDirCategory, int position) {
            mTitle.setText(isDirCategory ? R.string.text_directory : R.string.text_file);
            mIsDir = isDirCategory;
            if (isDirCategory && canGoBack()) {
                mGoBack.setVisibility(VISIBLE);
            } else {
                mGoBack.setVisibility(GONE);
            }
            if (isDirCategory) {
                mArrow.setRotation(mCurrentPageState.dirsCollapsed ? -90 : 0);
                mSortOrder.setImageResource(mExplorerItemList.isDirSortedAscending() ?
                        R.drawable.ic_ascending_order : R.drawable.ic_descending_order);
            } else {
                mArrow.setRotation(mCurrentPageState.filesCollapsed ? -90 : 0);
                mSortOrder.setImageResource(mExplorerItemList.isFileSortedAscending() ?
                        R.drawable.ic_ascending_order : R.drawable.ic_descending_order);
            }
        }

        void changeSortOrder() {
            if (mIsDir) {
                mSortOrder.setImageResource(mExplorerItemList.isDirSortedAscending() ?
                        R.drawable.ic_ascending_order : R.drawable.ic_descending_order);
                mExplorerItemList.setDirSortedAscending(!mExplorerItemList.isDirSortedAscending());
                sort(mExplorerItemList.getDirSortType(), mIsDir);
            } else {
                mSortOrder.setImageResource(mExplorerItemList.isFileSortedAscending() ?
                        R.drawable.ic_ascending_order : R.drawable.ic_descending_order);
                mExplorerItemList.setFileSortedAscending(!mExplorerItemList.isFileSortedAscending());
                sort(mExplorerItemList.getFileSortType(), mIsDir);
            }
        }

        void showSortOptions() {
            PopupMenu popupMenu = new PopupMenu(getContext(), mSort);
            popupMenu.inflate(R.menu.menu_sort_options);
            popupMenu.setOnMenuItemClickListener(ExplorerView.this);
            mDirSortMenuShowing = mIsDir;
            popupMenu.show();

        }

        void back() {
            if (canGoBack()) {
                goBack();
            }
        }

        void collapseOrExpand() {
            if (mIsDir) {
                mCurrentPageState.dirsCollapsed = !mCurrentPageState.dirsCollapsed;
            } else {
                mCurrentPageState.filesCollapsed = !mCurrentPageState.filesCollapsed;
            }
            mExplorerAdapter.notifyDataSetChanged();
        }
    }

    private static class ExplorerPageState {

        ExplorerPage page;

        boolean dirsCollapsed;

        boolean filesCollapsed;

        int scrollY;

        ExplorerPageState() {
        }

        ExplorerPageState(ExplorerPage page) {
            this.page = page;
        }
    }
}
