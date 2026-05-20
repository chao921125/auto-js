package net.cc.cca.ui.doc;

import android.webkit.WebView;

import net.cc.cca.Pref;
import net.cc.cca.R;
import net.cc.cca.databinding.ActivityDocumentationBinding;
import net.cc.cca.ui.BaseActivity;
import net.cc.cca.ui.widget.EWebView;

/**
 * Created by Stardust on 2017/10/24.
 */
public class DocumentationActivity extends BaseActivity {

    public static final String EXTRA_URL = "url";

    private ActivityDocumentationBinding binding;
    private android.webkit.WebView mWebView;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpViews();
    }

    void setUpViews() {
        setToolbarAsBack(getString(R.string.text_tutorial));
        mWebView = binding.ewebView.getWebView();
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url == null) {
            url = Pref.getDocumentationUrl() + "index.html";
        }
        mWebView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
