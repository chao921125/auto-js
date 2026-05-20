package net.cc.cca.ui.user;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import net.cc.cca.R;
import net.cc.cca.databinding.ActivityLoginBinding;
import net.cc.cca.network.NodeBB;
import net.cc.cca.network.UserService;
import net.cc.cca.ui.BaseActivity;
import com.stardust.theme.ThemeColorManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Stardust on 2017/9/20.
 */
public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpViews();
        setupClickListeners();
    }

    void setUpViews() {
        setToolbarAsBack(getString(R.string.text_login));
        ThemeColorManager.addViewBackground(binding.login);
    }

    private void setupClickListeners() {
        binding.login.setOnClickListener(v -> login());
        binding.forgotPassword.setOnClickListener(v -> forgotPassword());
    }

    void login() {
        String userName = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        if (!checkNotEmpty(userName, password)) {
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .progress(true, 0)
                .content(R.string.text_logining)
                .cancelable(false)
                .show();
        UserService.getInstance().login(userName, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.text_login_succeed, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        , error -> {
                            dialog.dismiss();
                            binding.password.setError(NodeBB.getErrorMessage(error, LoginActivity.this, R.string.text_login_fail));
                        });

    }

    void forgotPassword() {
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra(WebActivity.EXTRA_URL, NodeBB.BASE_URL + "reset");
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.text_reset_password));
        startActivity(intent);
    }

    private boolean checkNotEmpty(String userName, String password) {
        if (userName.isEmpty()) {
            binding.username.setError(getString(R.string.text_username_cannot_be_empty));
            return false;
        }
        if (password.isEmpty()) {
            binding.username.setError(getString(R.string.text_password_cannot_be_empty));
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_register) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
