package net.cc.cca.ui.main.drawer;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.cc.cca.R;
import net.cc.cca.databinding.DrawerMenuItemBinding;
import net.cc.cca.ui.widget.BindableViewHolder;
import net.cc.cca.ui.widget.PrefSwitch;
import net.cc.cca.ui.widget.SwitchCompat;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Stardust on 2017/12/10.
 */

public class DrawerMenuItemViewHolder extends BindableViewHolder<DrawerMenuItem> {

    private static final long CLICK_TIMEOUT = 1000;
    private DrawerMenuItemBinding binding;
    private boolean mAntiShake;
    private long mLastClickMillis;
    private DrawerMenuItem mDrawerMenuItem;

    public DrawerMenuItemViewHolder(View itemView) {
        super(itemView);
        binding = DrawerMenuItemBinding.bind(itemView);
        binding.sw.setOnCheckedChangeListener((buttonView, isChecked) -> onClick());
        itemView.setOnClickListener(v -> {
            if (binding.sw.getVisibility() == VISIBLE) {
                binding.sw.toggle();
            } else {
                onClick();
            }
        });
    }

    @Override
    public void bind(DrawerMenuItem item, int position) {
        mDrawerMenuItem = item;
        binding.icon.setImageResource(item.getIcon());
        binding.title.setText(item.getTitle());
        mAntiShake = item.antiShake();
        setSwitch(item);
        setProgress(item.isProgress());
        setNotifications(item.getNotificationCount());
    }

    private void setNotifications(int notificationCount) {
        if (notificationCount == 0) {
            binding.notifications.setVisibility(View.GONE);
        } else {
            binding.notifications.setVisibility(View.VISIBLE);
            binding.notifications.setText(String.valueOf(notificationCount));
        }
    }

    private void setSwitch(DrawerMenuItem item) {
        if (!item.isSwitchEnabled()) {
            binding.sw.setVisibility(GONE);
            return;
        }
        binding.sw.setVisibility(VISIBLE);
        int prefKey = item.getPrefKey();
        if (prefKey == 0) {
            binding.sw.setChecked(item.isChecked(), false);
            binding.sw.setPrefKey(null);
        } else {
            binding.sw.setPrefKey(itemView.getResources().getString(prefKey));
        }
    }

    private void onClick() {
        mDrawerMenuItem.setChecked(binding.sw.isChecked());
        if (mAntiShake && (System.currentTimeMillis() - mLastClickMillis < CLICK_TIMEOUT)) {
            Toast.makeText(itemView.getContext(), R.string.text_click_too_frequently, Toast.LENGTH_SHORT).show();
            binding.sw.setChecked(!binding.sw.isChecked(), false);
            return;
        }
        mLastClickMillis = System.currentTimeMillis();
        if (mDrawerMenuItem != null) {
            mDrawerMenuItem.performAction(this);
        }
    }

    private void setProgress(boolean onProgress) {
        binding.progressBar.setVisibility(onProgress ? VISIBLE : GONE);
        binding.icon.setVisibility(onProgress ? GONE : VISIBLE);
        binding.sw.setEnabled(!onProgress);
        itemView.setEnabled(!onProgress);
    }

    public SwitchCompat getSwitchCompat() {
        return binding.sw;
    }

}
