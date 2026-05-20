package net.cc.cca.ui.main.community;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.webkit.WebView;

import net.cc.cca.R;
import net.cc.cca.databinding.FragmentCommunityBinding;
import net.cc.cca.network.NodeBB;
import net.cc.cca.ui.main.QueryEvent;
import net.cc.cca.ui.main.ViewPagerFragment;
import com.stardust.util.BackPressedHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URLEncoder;

/**
 * Created by Stardust on 2017/8/22.
 */
public class CommunityFragment extends ViewPagerFragment implements BackPressedHandler {

    public static class LoadUrl {
        public final String url;

        public LoadUrl(String url) {
            this.url = url;
        }

    }

    public static class VisibilityChange {
        public final boolean visible;

        public VisibilityChange(boolean visible) {
            this.visible = visible;
        }
    }

    private static final String POSTS_PAGE_PATTERN = "[\\S\\s]+/topic/[0-9]+/[\\S\\s]+";

    private FragmentCommunityBinding binding;
    private WebView mWebView;

    public CommunityFragment() {
        super(0);
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public android.view.View onCreateView(android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
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
        mWebView = binding.ewebView.getWebView();
        String url = "https://www.autojs.org/";
        Bundle savedWebViewState = getArguments().getBundle("savedWebViewState");
        if (savedWebViewState != null) {
            mWebView.restoreState(savedWebViewState);
        } else {
            mWebView.loadUrl(url);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Bundle savedWebViewState = new Bundle();
        mWebView.saveState(savedWebViewState);
        getArguments().putBundle("savedWebViewState", savedWebViewState);
    }

    @Override
    public boolean onBackPressed(Activity activity) {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }


    @Override
    protected void onFabClick(FloatingActionButton fab) {
        if (isInPostsPage()) {
            mWebView.loadUrl("javascript:$('button[component=\"topic/reply\"]').click()");
        } else {
            mWebView.loadUrl("javascript:$('#new_topic').click()");
        }
    }

    @Subscribe
    public void loadUrl(LoadUrl loadUrl) {
        mWebView.loadUrl(NodeBB.url(loadUrl.url));
    }

    @Subscribe
    public void submitQuery(QueryEvent event) {
        if (!isShown() || event == QueryEvent.CLEAR) {
            return;
        }
        String query = URLEncoder.encode(event.getQuery());
        String url = String.format("http://www.autojs.org/search?term=%s&in=titlesposts", query);
        mWebView.loadUrl(url);
        event.collapseSearchView();
    }

    private boolean isInPostsPage() {
        String url = mWebView.getUrl();
        return url != null &&  url.matches(POSTS_PAGE_PATTERN);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPageShow() {
        super.onPageShow();
        EventBus.getDefault().post(new VisibilityChange(true));
    }

    @Override
    public void onPageHide() {
        super.onPageHide();
        EventBus.getDefault().post(new VisibilityChange(false));
    }
}
