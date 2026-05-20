package net.cc.cca.ui.log;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import net.cc.stardust.core.console.ConsoleView;
import net.cc.stardust.core.console.ConsoleImpl;

import net.cc.cca.R;
import net.cc.cca.databinding.ActivityLogBinding;
import net.cc.cca.autojs.AutoJs;
import net.cc.cca.ui.BaseActivity;

public class LogActivity extends BaseActivity {

    private ActivityLogBinding binding;
    private ConsoleImpl mConsoleImpl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyDayNightMode();
        setupViews();
        setupClickListeners();
    }

    void setupViews() {
        setToolbarAsBack(getString(R.string.text_log));
        mConsoleImpl = AutoJs.getInstance().getGlobalConsole();
        binding.console.setConsole(mConsoleImpl);
        binding.console.findViewById(R.id.input_container).setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        binding.fab.setOnClickListener(v -> clearConsole());
    }

    void clearConsole() {
        mConsoleImpl.clear();
    }
}
