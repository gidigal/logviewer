package dev.gidigal.logviewer;

import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.jar.JarApplicationConfigurationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.execution.jar.JarApplicationConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


class OpenInConsoleAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(OpenInConsoleAction.class);

    private String getSelectedFileName(@NotNull AnActionEvent event) {
        String res = "";
        Project project = event.getProject();
        if (project != null) {
            VirtualFile[] selectedFiles = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
            if (selectedFiles != null) {
                for (VirtualFile file : selectedFiles) {
                    // Process each selected file
                    res = file.getPath();
                }
            }
        }
        return res;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        String fileName = getSelectedFileName(event);
        String extension = fileName.isEmpty() ? "" : FilenameUtils.getExtension(fileName);
        event.getPresentation().setEnabledAndVisible(extension.equals("log"));
    }

    public static RunnerAndConfigurationSettings createJarRunConfiguration(
            Project project,
            String jarPath,
            String vmParameters,
            String programParameters
    ) {
        // Get the configuration factory for JarApplicationConfiguration
        ConfigurationFactory factory = JarApplicationConfigurationType.getInstance();

        // Create a RunnerAndConfigurationSettings
        RunnerAndConfigurationSettings settings = RunManager.getInstance(project)
                .createConfiguration("My Jar Configuration", factory);

        // Get the configuration and cast it to JarApplicationConfiguration
        JarApplicationConfiguration configuration =
                (JarApplicationConfiguration) settings.getConfiguration();

        // Set JAR configuration details
        configuration.setJarPath(jarPath);
        configuration.setVMParameters(vmParameters);
        configuration.setProgramParameters(programParameters);

        // Optionally, set as the selected configuration
        RunManager.getInstance(project).addConfiguration(settings);

        return settings;
    }

    private String getPrintLogsFilePath() {
        // Or get the specific plugin path (using your plugin ID)
        String pluginPath = PathManager.getPluginsPath() + "/dev.gidigal.logviewer";

        // Create a directory for your JAR if needed
        File jarDirectory = new File(pluginPath, "lib");
        if (!jarDirectory.exists()) {
            jarDirectory.mkdirs();
        }

        final String JAR_FILE_NAME = "printLogs-1.0-SNAPSHOT.jar";

        // Path to your JAR file
        File jarFile = new File(jarDirectory, JAR_FILE_NAME);

        // If the JAR doesn't exist yet, copy it from resources
        if (!jarFile.exists()) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(JAR_FILE_NAME)) {
                if ((is != null) && (jarFile.createNewFile())) {
                    FileOutputStream outputStream = new FileOutputStream(jarFile);
                    FileUtil.copy(is, outputStream);
                }
            } catch (IOException e) {
                LOG.error(e);
            }
        }
        // Now you can use the path to the JAR file in your configuration
        return jarFile.getAbsolutePath();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String fileName = getSelectedFileName(e);
        Project project = e.getProject();
        if (project != null) {
            final String printLogsJarFilePath = getPrintLogsFilePath();
            RunnerAndConfigurationSettings settings = createJarRunConfiguration(project, printLogsJarFilePath, "", fileName);
            Executor executor = DefaultRunExecutor.getRunExecutorInstance();
            ProgramRunnerUtil.executeConfiguration(settings, executor);
        }
    }
}