package net.cc.cca.external.shortcut;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;
import android.util.Log;

import java.util.Collections;
import java.util.List;

/**
 * Created by Stardust on 2017/10/25.
 */

public class ShortcutManager {


    private static ShortcutManager sInstance;
    private Context mContext;
    private android.content.pm.ShortcutManager mShortcutManager;


    public ShortcutManager(Context context) {
        mContext = context;
        mShortcutManager = (android.content.pm.ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
    }

    public static ShortcutManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ShortcutManager(context);
        }
        return sInstance;
    }


    public void addPinnedShortcut(CharSequence label, String id, Icon icon, Intent intent) {
        if (!mShortcutManager.isRequestPinShortcutSupported()) {
            Log.d("ShortcutManager", "pin shortcut is not supported");
            return;
        }
        ShortcutInfo shortcut = buildShortcutInfo(label, id, icon, intent);
        int req = getRequestCode(id);
        PendingIntent successCallback = PendingIntent.getBroadcast(mContext, req,
                mShortcutManager.createShortcutResultIntent(shortcut), PendingIntent.FLAG_IMMUTABLE);
        mShortcutManager.requestPinShortcut(shortcut, successCallback.getIntentSender());
    }

    private ShortcutInfo buildShortcutInfo(CharSequence label, String id, Icon icon, Intent intent) {
        return new ShortcutInfo.Builder(mContext, id)
                .setIntent(intent)
                .setShortLabel(label)
                .setLongLabel(label)
                .setIcon(icon)
                .build();
    }

    private int getRequestCode(String id) {
        return id.hashCode() >>> 16;
    }

    public void addDynamicShortcut(CharSequence label, String id, Icon icon, Intent intent) {
        ShortcutInfo shortcut = buildShortcutInfo(label, id, icon, intent);
        try {
            addDynamicShortcutUnchecked(shortcut);
        } catch (IllegalArgumentException shortcutsExceeded) {
            removeTheFirstShortcut();
            addDynamicShortcutUnchecked(shortcut);
        }
    }

    private void removeTheFirstShortcut() {
        List<ShortcutInfo> dynamicShortcuts = mShortcutManager.getDynamicShortcuts();
        mShortcutManager.removeDynamicShortcuts(Collections.singletonList(dynamicShortcuts.get(0).getId()));
    }

    private void addDynamicShortcutUnchecked(ShortcutInfo shortcut) {
        mShortcutManager.addDynamicShortcuts(Collections.singletonList(shortcut));
    }

}
