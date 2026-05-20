package net.cc.cca.external.tile;

import com.stardust.view.accessibility.NodeInfo;

import net.cc.cca.ui.floating.FullScreenFloatyWindow;
import net.cc.cca.ui.floating.layoutinspector.LayoutBoundsFloatyWindow;

public class LayoutBoundsTile extends LayoutInspectTileService {
    @Override
    protected FullScreenFloatyWindow onCreateWindow(NodeInfo capture) {
        return new LayoutBoundsFloatyWindow(capture) {
            @Override
            public void close() {
                super.close();
                inactive();
            }
        };
    }
}
