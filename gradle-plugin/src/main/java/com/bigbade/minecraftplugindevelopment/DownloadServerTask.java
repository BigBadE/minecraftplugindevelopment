package com.bigbade.minecraftplugindevelopment;

import com.bigbade.minecraftplugindevelopment.common.PaperLocator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class DownloadServerTask extends DefaultTask {
    @TaskAction
    public void downloadJar() {
        PluginDevelopmentExtension extension = getProject().getExtensions().findByType(PluginDevelopmentExtension.class);
        if (extension == null) {
            MinecraftPluginDevelopmentPlugin.LOGGER.info("Adding default PluginDevelopment extension");
            extension = new PluginDevelopmentExtension();
            getProject().getExtensions().add("minecraft", extension);
        }

        File serverFile = new File(getProject().getBuildDir(), "server");
        if (new File(serverFile, "paper-" +
                extension.build + ".jar").exists()) {
            return;
        }

        if(serverFile.listFiles() != null) {
            for (File subfile : serverFile.listFiles()) {
                if (subfile.getName().startsWith("paper-") && subfile.getName().endsWith(".jar")) {
                    subfile.delete();
                }
            }
        }

        serverFile.mkdirs();

        MinecraftPluginDevelopmentPlugin.LOGGER.info("Downloading Paper server!");

        String jarURL = PaperLocator.getDownloadURL(
                MinecraftPluginDevelopmentPlugin.LOGGER, extension.build, extension.localVersion);

        if (jarURL.equals("none")) {
            throw new IllegalStateException("Couldn't get paper build, set it manually!");
        }

        try {
            Files.copy(new URL(jarURL).openStream(),
                    new File(getProject().getBuildDir(), "server/paper-" + extension.build + ".jar").toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
