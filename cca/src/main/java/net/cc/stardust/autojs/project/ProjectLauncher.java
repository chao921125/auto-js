package net.cc.stardust.project;

import net.cc.stardust.ScriptEngineService;
import net.cc.stardust.execution.ExecutionConfig;
import net.cc.stardust.script.JavaScriptFileSource;

import java.io.File;

public class ProjectLauncher {

    private String mProjectDir;
    private File mMainScriptFile;
    private ProjectConfig mProjectConfig;

    public ProjectLauncher(String projectDir) {
        mProjectDir = projectDir;
        mProjectConfig = ProjectConfig.fromProjectDir(projectDir);
        mMainScriptFile = new File(mProjectDir, mProjectConfig.getMainScriptFile());
    }

    public void launch(ScriptEngineService service) {
        ExecutionConfig config = new ExecutionConfig();
        config.setWorkingDirectory(mProjectDir);
        config.getScriptConfig().setFeatures(mProjectConfig.getFeatures());
        service.execute(new JavaScriptFileSource(mMainScriptFile), config);
    }

}
