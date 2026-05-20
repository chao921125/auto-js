package net.cc.stardust.execution;

import net.cc.stardust.engine.LoopBasedJavaScriptEngine;
import net.cc.stardust.engine.ScriptEngine;
import net.cc.stardust.engine.ScriptEngineManager;
import net.cc.stardust.core.looper.Loopers;
import net.cc.stardust.script.JavaScriptSource;

/**
 * Created by Stardust on 2017/10/27.
 */

public class LoopedBasedJavaScriptExecution extends RunnableScriptExecution {

    public LoopedBasedJavaScriptExecution(ScriptEngineManager manager, ScriptExecutionTask task) {
        super(manager, task);
    }


    protected Object doExecution(final ScriptEngine engine) {
        engine.setTag(ScriptEngine.TAG_SOURCE, getSource());
        getListener().onStart(this);
        long delay = getConfig().getDelay();
        sleep(delay);
        final LoopBasedJavaScriptEngine javaScriptEngine = (LoopBasedJavaScriptEngine) engine;
        final long interval = getConfig().getInterval();
        javaScriptEngine.getRuntime().loopers.setMainLooperQuitHandler(new Loopers.LooperQuitHandler() {
            long times = getConfig().getLoopTimes() == 0 ? Integer.MAX_VALUE : getConfig().getLoopTimes();

            @Override
            public boolean shouldQuit() {
                times--;
                if (times > 0) {
                    sleep(interval);
                    javaScriptEngine.execute(getSource());
                    return false;
                }
                javaScriptEngine.getRuntime().loopers.setMainLooperQuitHandler(null);
                return true;
            }
        });
        javaScriptEngine.execute(getSource());
        return null;
    }

    @Override
    public JavaScriptSource getSource() {
        return (JavaScriptSource) super.getSource();
    }

}
