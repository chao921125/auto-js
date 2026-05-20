package net.cc.stardust.runtime.api;

import net.cc.stardust.ScriptEngineService;
import net.cc.stardust.engine.JavaScriptEngine;
import net.cc.stardust.execution.ExecutionConfig;
import net.cc.stardust.execution.ScriptExecution;
import net.cc.stardust.runtime.ScriptRuntime;
import net.cc.stardust.script.AutoFileSource;
import net.cc.stardust.script.JavaScriptFileSource;
import net.cc.stardust.script.StringScriptSource;

import java.lang.ref.WeakReference;

/**
 * Created by Stardust on 2017/8/4.
 */

public class Engines {

    private ScriptEngineService mEngineService;
    private WeakReference<JavaScriptEngine> mScriptEngine;
    private WeakReference<ScriptRuntime> mScriptRuntime;

    public Engines(ScriptEngineService engineService, ScriptRuntime scriptRuntime) {
        mEngineService = engineService;
        mScriptRuntime = new WeakReference<>(scriptRuntime);
    }

    public ScriptExecution execScript(String name, String script, ExecutionConfig config) {
        return mEngineService.execute(new StringScriptSource(name, script), config);
    }

    public ScriptExecution execScriptFile(String path, ExecutionConfig config) {
        return mEngineService.execute(new JavaScriptFileSource(mScriptRuntime.get().files.path(path)), config);
    }

    public ScriptExecution execAutoFile(String path, ExecutionConfig config) {
        return mEngineService.execute(new AutoFileSource(mScriptRuntime.get().files.path(path)), config);
    }

    public Object all() {
        return mScriptRuntime.get().bridges.toArray(mEngineService.getEngines());
    }

    public int stopAll() {
        return mEngineService.stopAll();
    }

    public void stopAllAndToast() {
        mEngineService.stopAllAndToast();
    }


    public void setCurrentEngine(JavaScriptEngine engine) {
        if (mScriptEngine != null)
            throw new IllegalStateException();
        mScriptEngine = new WeakReference<>(engine);
    }

    public JavaScriptEngine myEngine() {
        return mScriptEngine.get();
    }
}
