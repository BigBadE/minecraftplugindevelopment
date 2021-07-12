package com.bigbade.minecraftplugindevelopment;

public final class MavenPom {
    public static final String MAVEN_POM = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
              <groupId>com.bigbade</groupId>
              <artifactId>mcpatched</artifactId>
              <version>%s</version>
              <name>MCPatched</name>
              <description>Paper patched with Vanilla mappings, stored in your maven repo and converted back
              to Spigot mappings automatically on build by MinecraftPluginDevelopment.
              This jar contains copyrighted material from Mojang, redistribution or use outside of development is illegal.
              It also most likely won't even run.</description>
            </project>
            """;

    private MavenPom() {}
}
