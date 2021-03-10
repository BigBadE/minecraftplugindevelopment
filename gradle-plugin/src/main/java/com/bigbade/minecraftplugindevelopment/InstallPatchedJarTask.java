package com.bigbade.minecraftplugindevelopment;

import com.bigbade.specialsourcesrg.Jar;
import com.bigbade.specialsourcesrg.JarMapping;
import com.bigbade.specialsourcesrg.JarRemapper;
import io.sigpipe.jbsdiff.InvalidHeaderException;
import io.sigpipe.jbsdiff.Patch;
import org.apache.commons.compress.compressors.CompressorException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import se.llbit.json.JsonObject;
import se.llbit.json.JsonParser;
import se.llbit.json.JsonValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InstallPatchedJarTask extends DefaultTask {
    private static final String VERSION_MANIFEST = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    private static void patchAndMap(File jar, byte[] jarBytes, File paperJar, String mappingUrl) {
        try (JarFile paper = new JarFile(paperJar);
             DigestOutputStream jarOutput = new DigestOutputStream(new FileOutputStream(new File(jar.getParentFile(),
                     "mcpatched-temp.jar")),
                     MessageDigest.getInstance("SHA-256"))) {
            JarEntry propertiesEntry = paper.getJarEntry("patch.properties");
            Properties properties = new Properties();
            properties.load(paper.getInputStream(propertiesEntry));
            JarEntry patch = paper.getJarEntry(properties.getProperty("patch", "paperMC.patch"));
            byte[] patchBytes = readFully(paper.getInputStream(patch));

            Patch.patch(jarBytes, patchBytes, jarOutput);

            byte[] digest = jarOutput.getMessageDigest().digest();
            if (!Arrays.equals(digest,
                    getBytesOfHex(properties.getProperty("patchedHash")))) {
                throw new IllegalStateException("Hash of patched file is wrong! (" + bytesToHex(digest) + " vs "
                        + properties.getProperty("patchedHash") + ")");
            }

            //JarMapping mapping = new JarMapping();
            //mapping.loadMappings(new URL(mappingUrl).openStream());
            //JarRemapper remapper = new JarRemapper(mapping);
            //remapper.remapJar(Jar.init(new File(jar.getParentFile(),
            //        "mcpatched-temp.jar")), jar);
        } catch (IOException | NoSuchAlgorithmException | CompressorException | InvalidHeaderException e) {
            jar.delete();
            e.printStackTrace();
        }
    }

    private static byte[] readFully(InputStream in) throws IOException {
        try {
            // In a test this was 12 ms quicker than a ByteBuffer
            // and for some reason that matters here.
            byte[] buffer = new byte[16 * 1024];
            int off = 0;
            int read;
            while ((read = in.read(buffer, off, buffer.length - off)) != -1) {
                off += read;
                if (off == buffer.length) {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }
            }
            return Arrays.copyOfRange(buffer, 0, off);
        } finally {
            in.close();
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

    private static byte[] downloadAndVerifyFile(String filePath, String sha1) {
        try (InputStream inputStream = new URL(filePath).openStream()) {
            MinecraftPluginDevelopmentPlugin.LOGGER.info("Downloading from {}", filePath);
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-1"));
            byte[] output = readFully(digestInputStream);
            if (!Arrays.equals(digestInputStream.getMessageDigest().digest(), getBytesOfHex(sha1))) {
                throw new IllegalStateException("Problem downloading server jar, sha1 hash doesn't match!");
            }
            return output;
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private static byte[] getBytesOfHex(String hex) {
        int length = hex.length();
        byte[] out = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            int h = hexToBin(hex.charAt(i));
            int l = hexToBin(hex.charAt(i + 1));

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    private static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    private static int hexToBin(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return -1;
    }

    @TaskAction
    public void installMappedNMSJar() {
        PluginDevelopmentExtension extension = getProject().getExtensions().findByType(PluginDevelopmentExtension.class);
        assert extension != null;
        if (!extension.nmsHelper) {
            return;
        }
        String mavenRepo = getProject().getRepositories().mavenLocal().getUrl().getPath();
        if (mavenRepo == null) {
            MinecraftPluginDevelopmentPlugin.LOGGER.error("No maven local repo found!");
            return;
        }
        File mavenOutput = new File(mavenRepo + File.separatorChar + "com" + File.separatorChar + "bigbade"
                + File.separatorChar + "mcpatched" + File.separatorChar + extension.localVersion + File.separatorChar
                + "mcpatched-" + extension.localVersion + ".jar");
        Path versionFile = new File(mavenOutput.getParentFile(), "version.txt").toPath();
        if (mavenOutput.exists()) {
            try {
                List<String> lines = Files.readAllLines(versionFile);
                if (lines.size() == 1 && lines.get(0).equals(extension.build)) {
                    return;
                }
            } catch (IOException ignored) {
                //Ignore
            }
        }
        MinecraftPluginDevelopmentPlugin.LOGGER.info("Downloading version server jar");
        try (JsonParser parser = new JsonParser(new URL(VERSION_MANIFEST).openStream())) {
            JsonObject json = (JsonObject) parser.parse();
            for (JsonValue versionValue : json.get("versions").asArray()) {
                JsonObject version = versionValue.asObject();
                if (!version.get("id").stringValue("nope").equals(extension.localVersion)) {
                    continue;
                }
                String url = version.get("url").stringValue("whoops");
                JsonObject server = getVersionObject(url);
                byte[] jarBytes = downloadAndVerifyFile(
                        server.get("server").asObject().get("url").asString("no server"),
                        server.get("server").asObject().get("sha1").asString("no sha1 hash"));
                mavenOutput.getParentFile().mkdirs();
                patchAndMap(mavenOutput, jarBytes, new File(getProject().getBuildDir(),
                                "server/paper-" + extension.build + ".jar"),
                        server.get("server_mappings").asObject().get("url").asString("No mappings!"));
                Files.write(versionFile, extension.build.getBytes(StandardCharsets.UTF_8));
                return;
            }
            MinecraftPluginDevelopmentPlugin.LOGGER.error("Could not find version {}", extension.localVersion);
        } catch (IOException | JsonParser.SyntaxError e) {
            mavenOutput.delete();
            MinecraftPluginDevelopmentPlugin.LOGGER.error("Could not read version manifest", e);
            e.printStackTrace();
        }
    }
}
