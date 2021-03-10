package com.bigbade.minecraftplugindevelopment;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import se.llbit.json.JsonArray;
import se.llbit.json.JsonParser;
import se.llbit.json.JsonValue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DownloadServerTask extends DefaultTask {
    private static final String PAPER_API_LINK = "https://papermc.io/api/v2/projects/paper/versions/";

    public static String getDownloadURL(PluginDevelopmentExtension extension) {
        if (extension.build.equals("none")) {
            return "none";
        }
        try (JsonParser parser = new JsonParser(
                new URL(PAPER_API_LINK + extension.localVersion + "/builds/" + extension.build)
                        .openStream())) {
            JsonValue value = parser.parse();
            String download = value.asObject().get("downloads").asObject().get("application").asObject()
                    .get("name").asString("none");

            return PAPER_API_LINK + extension.localVersion + "/builds/" + extension.build + "/downloads/" + download;
        } catch (IOException | JsonParser.SyntaxError e) {
            MinecraftPluginDevelopmentPlugin.LOGGER.error("Failure getting paper download jar", e);
        }
        return "none";
    }

    public static String getBuild(String version) {
        try (JsonParser parser = new JsonParser(
                new URL(PAPER_API_LINK + version).openStream())) {
            JsonValue value = parser.parse();
            JsonArray builds = value.asObject().get("builds").asArray();
            return builds.get(builds.size() - 1).toString();
        } catch (IOException | JsonParser.SyntaxError e) {
            MinecraftPluginDevelopmentPlugin.LOGGER.error("Failure getting paper download jar", e);
        }
        return "none";
    }

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

        for(File subfile : serverFile.listFiles()) {
            if(subfile.getName().startsWith("paper-") && subfile.getName().endsWith(".jar")) {
                subfile.delete();
            }
        }

        MinecraftPluginDevelopmentPlugin.LOGGER.info("Downloading Paper server!");

        String jarURL = DownloadServerTask.getDownloadURL(extension);

        if (jarURL.equals("none")) {
            throw new IllegalStateException("Couldn't get paper build, set it manually!");
        }
        try {
            Files.copy(new URL(jarURL).openStream(),
                    new File(getProject().getBuildDir(), "server/paper-" + extension.build + ".jar").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
