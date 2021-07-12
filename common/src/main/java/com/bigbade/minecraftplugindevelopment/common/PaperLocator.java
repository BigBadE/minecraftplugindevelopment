package com.bigbade.minecraftplugindevelopment.common;

import org.slf4j.Logger;
import se.llbit.json.JsonArray;
import se.llbit.json.JsonParser;
import se.llbit.json.JsonValue;

import java.io.IOException;
import java.net.URL;

public final class PaperLocator {
    private static final String PAPER_API_LINK = "https://papermc.io/api/v2/projects/paper/versions/";

    private PaperLocator() {}

    public static String getDownloadURL(Logger logger, String build, String localVersion) {
        if (build.equals("none")) {
            return "none";
        }
        try (JsonParser parser = new JsonParser(
                new URL(PAPER_API_LINK + localVersion + "/builds/" + build)
                        .openStream())) {
            JsonValue value = parser.parse();
            String download = value.asObject().get("downloads").asObject().get("application").asObject()
                    .get("name").asString("none");

            return PAPER_API_LINK + localVersion + "/builds/" + build + "/downloads/" + download;
        } catch (IOException | JsonParser.SyntaxError e) {
            logger.error("Failure getting paper download jar", e);
        }
        return "none";
    }

    public static String getBuild(Logger logger, String version) {
        try (JsonParser parser = new JsonParser(
                new URL(PAPER_API_LINK + version).openStream())) {
            JsonValue value = parser.parse();
            JsonArray builds = value.asObject().get("builds").asArray();
            return builds.get(builds.size() - 1).toString();
        } catch (IOException | JsonParser.SyntaxError e) {
            logger.error("Failure getting paper download jar", e);
        }
        return "none";
    }
}
