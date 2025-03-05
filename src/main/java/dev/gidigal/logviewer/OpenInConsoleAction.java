package dev.gidigal.logviewer;

import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.ConfigurationFactory;

import java.util.Arrays;
import java.lang.reflect.Method;


class OpenInConsoleAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

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
        // First, find the JarApplicationConfiguration's ConfigurationType
        ConfigurationType jarConfigurationType =
                Arrays.stream(ConfigurationType.CONFIGURATION_TYPE_EP.getExtensions())
                        .filter(type -> type.getClass().getName().contains("JarApplicationConfigurationType"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Could not find JAR Configuration Type"));

        // Get the first configuration factory for this type
        ConfigurationFactory factory = jarConfigurationType.getConfigurationFactories()[0];

        // Create the run configuration
        RunnerAndConfigurationSettings settings =
                RunManager.getInstance(project).createConfiguration(
                        "My Jar Configuration",
                        factory
                );

        // Get the run configuration
        RunConfiguration runConfiguration = settings.getConfiguration();

        // Use reflection to set JAR configuration properties
        try {
            Method setJarPathMethod = runConfiguration.getClass().getMethod("setJarPath", String.class);
            setJarPathMethod.invoke(runConfiguration, jarPath);

            Method setVmParametersMethod = runConfiguration.getClass().getMethod("setVMParameters", String.class);
            setVmParametersMethod.invoke(runConfiguration, vmParameters);

            Method setProgramParametersMethod = runConfiguration.getClass().getMethod("setProgramParameters", String.class);
            setProgramParametersMethod.invoke(runConfiguration, programParameters);

            // Add the configuration
            RunManager.getInstance(project).addConfiguration(settings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure JAR run configuration", e);
        }

        return settings;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        System.out.println("TEST !!!");
        String fileName = getSelectedFileName(e);
        Project project = e.getProject();
        if (project != null) {
            RunnerAndConfigurationSettings settings = createJarRunConfiguration(project, "D:\\meital\\printLogs\\target\\printLogs-1.0-SNAPSHOT.jar", "", fileName);
            Executor executor = DefaultRunExecutor.getRunExecutorInstance();
            ProgramRunnerUtil.executeConfiguration(settings, executor);
        }
    }
}