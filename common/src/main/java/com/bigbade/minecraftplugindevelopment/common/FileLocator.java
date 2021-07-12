package com.bigbade.minecraftplugindevelopment.common;

import lombok.Getter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import se.llbit.json.JsonObject;
import se.llbit.json.JsonParser;
import se.llbit.json.JsonValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLocator {
    private static final String VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String SPIGOT_VERSION_FILE = "https://hub.spigotmc.org/versions/%s.json";
    private static final String SPIGOT_MAPPINGS = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/" +
            "browse/mappings?at=%s&raw";
    private static final String SPIGOT_MAPPING_FILE = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/" +
            "browse/mappings/%s?at=%s&raw";
    private static final String CRAFTBUKKIT_POM_FILE = "https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/" +
            "browse/pom.xml?at=%s&raw";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%s");
    private static final Pattern TAB_PATTERN = Pattern.compile("\t");

    private final Logger logger;
    private final String version;
    private final String commitHash;

    @Getter
    private String server;
    @Getter
    private String serverHash;
    @Getter
    private String craftbukkitVersion;
    @Getter
    private String vanillaMappingsFile;

    public FileLocator(Logger logger, String version) {
        this.logger = logger;
        this.version = version;
        String commit = null;
        String craftbukkitCommit = null;
        try (JsonParser parser = new JsonParser(
                new URL(replacePlaceholders(SPIGOT_VERSION_FILE, version)).openStream())) {
            JsonObject refs = parser.parse().asObject().get("refs").asObject();
            commit = refs.get("BuildData").asString("No BuildData!");
            craftbukkitCommit = refs.get("CraftBukkit").asString("No CraftBukkit!");
        } catch (IOException | JsonParser.SyntaxError e) {
            e.printStackTrace();
        }
        this.commitHash = commit;
        getVanillaFiles();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(
                replacePlaceholders(CRAFTBUKKIT_POM_FILE, craftbukkitCommit)).openStream(), StandardCharsets.UTF_8))) {
            for(int i = 0; i < 6; i++) {
                reader.readLine();
            }
            String temp = reader.readLine();
            craftbukkitVersion = temp.substring("    <version>".length(), temp.length()-"-SNAPSHOT</version>".length());
        } catch (IOException e) {
            logger.error("Error reading craftbukkit pom", e);
            e.printStackTrace();
        }
    }

    public void getVanillaFiles() {
        try (JsonParser parser = new JsonParser(new URL(VERSION_MANIFEST).openStream())) {
            JsonObject json = (JsonObject) parser.parse();
            for (JsonValue versionValue : json.get("versions").asArray()) {
                JsonObject versionObject = versionValue.asObject();
                if (!versionObject.get("id").stringValue("nope").equals(version)) {
                    continue;
                }
                String url = versionObject.get("url").stringValue("whoops");
                JsonObject serverObject = getVersionObject(url);
                assert serverObject != null;
                this.server = serverObject.get("server").asObject().get("url").asString("no server");
                this.serverHash = serverObject.get("server").asObject().get("sha1").asString("no sha1 hash");
                this.vanillaMappingsFile = serverObject.get("server_mappings").asObject()
                        .get("url").asString("No mappings!");
            }
        } catch (IOException | JsonParser.SyntaxError e) {
            logger.error("Error reading version manifest", e);
            e.printStackTrace();
        }
    }

    private static JsonObject getVersionObject(String url) {
        try (JsonParser parser = new JsonParser(new URL(url).openStream())) {
            return parser.parse().asObject().get("downloads").asObject();
        } catch (IOException | JsonParser.SyntaxError e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getMappingFiles() {
        List<String> files = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new URL(replacePlaceholders(SPIGOT_MAPPINGS, commitHash)).openStream(), StandardCharsets.UTF_8))) {
            files.add(TAB_PATTERN.split(reader.readLine())[1]);
        } catch (IOException e) {
            logger.error("Error getting Spigot mappings", e);
            e.printStackTrace();
        }
        return files;
    }

    @SneakyThrows
    public URL getMappingFile(String file) {
        return new URL(replacePlaceholders(SPIGOT_MAPPING_FILE, file, commitHash));
    }

    public String replacePlaceholders(String base, String... placeholders) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(base);
        StringBuffer output = new StringBuffer();
        int i = 0;
        while (matcher.find()) {
            if (matcher.group(0).equals("%s")) {
                matcher.appendReplacement(output, placeholders[i++]);
            }
        }
        matcher.appendTail(output);
        return output.toString();
    }
}
