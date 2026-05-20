package net.cc.cca.ui.edit.toolbar;

import net.cc.cca.R;

import java.util.Arrays;
import java.util.List;

public class NormalToolbarFragment extends ToolbarFragment {

    public NormalToolbarFragment() {
    }

    @Override
    public List<Integer> getMenuItemIds() {
        return Arrays.asList(R.id.run, R.id.undo, R.id.redo, R.id.save);
    }
}
