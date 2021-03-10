package com.bigbade.minecraftplugindevelopment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class MinecraftPluginDevelopmentPlugin implements Plugin<Project> {
    public static final Logger LOGGER = LoggerFactory.getLogger(MinecraftPluginDevelopmentPlugin.class);

    public void apply(Project project) {
        project.getExtensions().create("minecraft", PluginDevelopmentExtension.class);

        PluginDevelopmentExtension extension = project.getExtensions().findByType(PluginDevelopmentExtension.class);
        assert extension != null;
        project.afterEvaluate(found -> {
            if(extension.build == null) {
                extension.build = DownloadServerTask.getBuild(extension.localVersion);
            }
            try {
                setupDependencies(extension, project);
            } catch (MalformedURLException | URISyntaxException e) {
                LOGGER.error("Error getting server software.", e);
            }
        });

        File serverFile = new File(project.getBuildDir(), "server");
        File vanillaFile = new File(serverFile, "server/cache/mojang_" + extension.localVersion + ".jar");
        if(serverFile.exists() && !vanillaFile.exists()) {
            serverFile.delete();
        }
        serverFile.mkdir();

        if(extension.minecraftPluginDevelopment) {
            project.getTasks().create("patchJar", InstallPatchedJarTask.class, task -> {
                task.setGroup("Build");
                task.dependsOn("setupServer");
                task.getOutputs().upToDateWhen(found -> false);
            });
        }
        project.getTasks().create("setupServer", DownloadServerTask.class, task -> task.setGroup("Deployment"));

        project.getTasks().create("runServer", StartServerTask.class, task -> {
            task.setMain("-jar");
            task.dependsOn("setupServer");
            task.getOutputs().upToDateWhen(found -> false);
            task.setGroup("Deployment");
        });
    }

    public void setupDependencies(PluginDevelopmentExtension extension, Project project)
            throws MalformedURLException, URISyntaxException {
        URI url;
        String dependency;
        if ("spigot".equalsIgnoreCase(extension.serverSoftware)) {
            url = new URL("http" + (extension.useHTTPS ? "s" : "")
                    + "://hub.spigotmc.org/nexus/content/repositories/snapshots/").toURI();
            dependency = "org.spigotmc:spigot-api:" + extension.localVersion + "-R0.1-SNAPSHOT";
        } else if ("paper".equalsIgnoreCase(extension.serverSoftware)) {
            url = new URL("http" + (extension.useHTTPS ? "s" : "")
                    + "://papermc.io/repo/repository/maven-public/").toURI();
            dependency = "com.destroystokyo.paper:paper-api:" + extension.localVersion + "-R0.1-SNAPSHOT";
        } else {
            throw new IllegalArgumentException("Unknown server software: " + extension.serverSoftware);
        }
        project.getRepositories().jcenter();
        project.getRepositories().maven(maven -> maven.setUrl(url));
        URI jitpack = new URL("http" + (extension.useHTTPS ? "s" : "")
                + "://jitpack.io").toURI();
        project.getRepositories().maven(maven -> maven.setUrl(jitpack));
        project.getDependencies().add("compileOnly", dependency);
        if(extension.minecraftPluginDevelopment) {
            project.getDependencies().add("compileOnly", "com.bigbade.minecraftplugindevelopment:core:"
                    + extension.pluginDevelopmentVersion);
            project.getDependencies().add("annotationProcessor", "com.bigbade.minecraftplugindevelopment:core:"
                    + extension.pluginDevelopmentVersion);
            if(extension.nmsHelper) {
                project.getDependencies().add("compileOnly", "com.bigbade:mcpatched:"
                        + extension.localVersion);
            }
        }
        if(extension.mockBukkit) {
            project.getDependencies().add("testImplementation", "com.github.seeseemelk:MockBukkit-v1.16:0.5.0");
        }
    }
}
