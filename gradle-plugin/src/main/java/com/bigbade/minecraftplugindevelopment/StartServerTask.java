package com.bigbade.minecraftplugindevelopment;

import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.bundling.Jar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class StartServerTask extends JavaExec {
    @Override
    public void exec() {
        PluginDevelopmentExtension extension = getProject().getExtensions().findByType(PluginDevelopmentExtension.class);
        assert extension != null;

        if(!extension.eula) {
            throw new IllegalStateException("You must agree to the EULA of Minecraft! " +
                    "(https://account.mojang.com/documents/minecraft_eula)" +
                    "Set eula to true in the minecraft extension to agree to the EULA.");
        }
        new File(getProject().getBuildDir(), "server/plugins").mkdir();
        File eulaFile = new File(getProject().getBuildDir(), "server/eula.txt");

        try {
            if(!eulaFile.exists()) {
                Files.write(eulaFile.toPath(), ("#Agreed on " + new Date().toString() + "\neula=true")
                                .getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            }

            Jar jarTask = (Jar) getProject().getTasks().getByName("jar");

            Files.copy(jarTask.getArchiveFile().get().getAsFile().toPath(),
                    new File(getProject().getBuildDir(), "server/plugins/" +
                            jarTask.getArchiveFileName().get()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            MinecraftPluginDevelopmentPlugin.LOGGER.error("Error moving plugin into server", e);
        }

        setWorkingDir(new File(getProject().getBuildDir(), "server"));
        setArgsString("\"" + new File(getProject().getBuildDir(), "server/paper-"
                + extension.build + ".jar").getAbsolutePath() + "\"" + ((extension.serverGui) ? "" : " -nogui"));
        setEnableAssertions(true);
        if(extension.debug) {
            setDebug(true);
        }
        setStandardInput(System.in);
        super.exec();
    }
}
