package net.cc.cca.external.tile;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import com.stardust.app.GlobalAppContext;
import com.stardust.view.accessibility.AccessibilityService;
import com.stardust.view.accessibility.LayoutInspector;
import com.stardust.view.accessibility.NodeInfo;

import net.cc.cca.R;
import net.cc.cca.autojs.AutoJs;
import net.cc.cca.tool.AccessibilityServiceTool;
import net.cc.cca.ui.floating.FloatyWindowManger;
import net.cc.cca.ui.floating.FullScreenFloatyWindow;

public abstract class LayoutInspectTileService extends TileService implements LayoutInspector.CaptureAvailableListener {

    private boolean mCapturing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(getClass().getName(), "onCreate");
        AutoJs.getInstance().getLayoutInspector().addCaptureAvailableListener(this);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Log.d(getClass().getName(), "onStartListening");
        inactive();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(getClass().getName(), "onDestroy");
        AutoJs.getInstance().getLayoutInspector().removeCaptureAvailableListener(this);
    }

    @Override
    public void onClick() {
        super.onClick();
        Log.d(getClass().getName(), "onClick");
        sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        if (AccessibilityService.Companion.getInstance() == null) {
            Toast.makeText(this, R.string.text_no_accessibility_permission_to_capture, Toast.LENGTH_SHORT).show();
            AccessibilityServiceTool.goToAccessibilitySetting();
            inactive();
            return;
        }
        mCapturing = true;
        GlobalAppContext.postDelayed(() ->
                        AutoJs.getInstance().getLayoutInspector().captureCurrentWindow()
                , 1000);
    }

    protected void inactive() {
        Tile qsTile = getQsTile();
        if (qsTile == null)
            return;
        qsTile.setState(Tile.STATE_INACTIVE);
        qsTile.updateTile();
    }

    @Override
    public void onCaptureAvailable(NodeInfo capture) {
        Log.d(getClass().getName(), "onCaptureAvailable: capturing = " + mCapturing);
        if (!mCapturing) {
            return;
        }
        mCapturing = false;
        GlobalAppContext.post(() -> {
            FullScreenFloatyWindow window = onCreateWindow(capture);
            if (!FloatyWindowManger.addWindow(getApplicationContext(), window)) {
                inactive();
            }
        });

    }

    protected abstract FullScreenFloatyWindow onCreateWindow(NodeInfo capture);
}
