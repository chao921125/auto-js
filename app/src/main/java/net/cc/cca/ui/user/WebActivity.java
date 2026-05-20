package net.cc.cca.ui.user;

import android.content.Intent;
import androidx.annotation.NonNull;

import com.stardust.app.OnActivityResultDelegate;
import net.cc.cca.R;
import net.cc.cca.databinding.ActivityWebBinding;
import net.cc.cca.ui.BaseActivity;
import net.cc.cca.ui.widget.EWebView;

/**
 * Created by Stardust on 2017/10/26.
 */
public class WebActivity extends BaseActivity implements OnActivityResultDelegate.DelegateHost {

    public static final String EXTRA_URL = "url";

    private OnActivityResultDelegate.Mediator mMediator = new OnActivityResultDelegate.Mediator();
    private ActivityWebBinding binding;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupViews();
    }

    void setupViews() {
        setToolbarAsBack(getIntent().getStringExtra(Intent.EXTRA_TITLE));
        binding.ewebView.getWebView().loadUrl(getIntent().getStringExtra(EXTRA_URL));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mMediator.onActivityResult(requestCode, resultCode, data);
    }

    @NonNull
    @Override
    public OnActivityResultDelegate.Mediator getOnActivityResultDelegateMediator() {
        return mMediator;
    }
}
