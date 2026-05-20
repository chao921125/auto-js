package net.cc.stardust.core.ui.inflater.inflaters;

import androidx.annotation.Nullable;

import net.cc.stardust.core.graphics.ScriptCanvasView;
import net.cc.stardust.core.ui.inflater.ResourceParser;
import net.cc.stardust.core.ui.inflater.ViewCreator;
import net.cc.stardust.runtime.ScriptRuntime;

/**
 * Created by Stardust on 2018/3/16.
 */

public class CanvasViewInflater extends BaseViewInflater<ScriptCanvasView> {

    private ScriptRuntime mScriptRuntime;

    public CanvasViewInflater(ResourceParser resourceParser, ScriptRuntime runtime) {
        super(resourceParser);
        mScriptRuntime = runtime;
    }

    @Nullable
    @Override
    public ViewCreator<ScriptCanvasView> getCreator() {
        return (context, attrs) -> new ScriptCanvasView(context, mScriptRuntime);
    }
}
