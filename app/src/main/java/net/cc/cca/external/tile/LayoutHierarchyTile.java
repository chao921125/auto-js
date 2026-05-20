package net.cc.cca.external.tile;

import com.stardust.view.accessibility.NodeInfo;

import net.cc.cca.ui.floating.FullScreenFloatyWindow;
import net.cc.cca.ui.floating.layoutinspector.LayoutHierarchyFloatyWindow;

public class LayoutHierarchyTile extends LayoutInspectTileService {
    @Override
    protected FullScreenFloatyWindow onCreateWindow(NodeInfo capture) {
        return new LayoutHierarchyFloatyWindow(capture) {
            @Override
            public void close() {
                super.close();
                inactive();
            }
        };
    }
}
