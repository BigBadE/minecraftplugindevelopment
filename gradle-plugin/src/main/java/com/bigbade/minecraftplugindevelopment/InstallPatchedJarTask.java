package com.bigbade.minecraftplugindevelopment;

import com.bigbade.minecraftplugindevelopment.common.FileLocator;
import com.bigbade.minecraftplugindevelopment.common.FileRemapper;
import io.sigpipe.jbsdiff.InvalidHeaderException;
import io.sigpipe.jbsdiff.Patch;
import org.apache.commons.compress.compressors.CompressorException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

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
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    private static void patchJar(byte[] jar, File outputDirectory, File paperJar) {
        try (JarFile paper = new JarFile(paperJar);
             DigestOutputStream jarOutput = new DigestOutputStream(new FileOutputStream(new File(outputDirectory,
                     "mcpatched-temp.jar")),
                     MessageDigest.getInstance("SHA-256"))) {
            JarEntry propertiesEntry = paper.getJarEntry("patch.properties");
            Properties properties = new Properties();
            properties.load(paper.getInputStream(propertiesEntry));
            JarEntry patch = paper.getJarEntry(properties.getProperty("patch", "paperMC.patch"));
            byte[] patchBytes = FileRemapper.readFully(paper.getInputStream(patch));

            Patch.patch(jar, patchBytes, jarOutput);

            byte[] digest = jarOutput.getMessageDigest().digest();
            if (!Arrays.equals(digest,
                    getBytesOfHex(properties.getProperty("patchedHash")))) {
                throw new IllegalStateException("Hash of patched file is wrong! (" + bytesToHex(digest) + " vs "
                        + properties.getProperty("patchedHash") + ")");
            }
        } catch (IOException | NoSuchAlgorithmException | CompressorException | InvalidHeaderException e) {
            e.printStackTrace();
        }
    }

    private static byte[] downloadAndVerifyFile(String filePath, String sha1) {
        try (InputStream inputStream = new URL(filePath).openStream()) {
            MinecraftPluginDevelopmentPlugin.LOGGER.info("Downloading from {}", filePath);
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, MessageDigest.getInstance("SHA-1"));
            byte[] output = FileRemapper.readFully(digestInputStream);
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
    public void installMappedNMSJar() throws IOException {
        PluginDevelopmentExtension extension = getProject().getExtensions().findByType(PluginDevelopmentExtension.class);
        assert extension != null;
        if (!extension.nmsHelper) {
            return;
        }
        MinecraftPluginDevelopmentPlugin.LOGGER.warn("Mapped NMS Jar is a WIP feature, report issues to out GitHub");
        String mavenRepo = getProject().getRepositories().mavenLocal().getUrl().getPath();
        if (mavenRepo == null) {
            MinecraftPluginDevelopmentPlugin.LOGGER.error("No maven local repo found!");
            return;
        }
        File mavenOutput = new File(mavenRepo + File.separatorChar + "com" + File.separatorChar + "bigbade"
                + File.separatorChar + "mcpatched" + File.separatorChar + extension.localVersion + "-" + extension.build
                + File.separatorChar + "mcpatched-" + extension.localVersion + "-" + extension.build + ".jar");
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
        FileLocator locator = new FileLocator(MinecraftPluginDevelopmentPlugin.LOGGER, extension.localVersion);

        mavenOutput.getParentFile().mkdirs();
        patchJar(downloadAndVerifyFile(locator.getServer(), locator.getServerHash()),
                mavenOutput.getParentFile(),
                new File(getProject().getBuildDir(), "server/paper-" + extension.build + ".jar"));

        new FileRemapper(MinecraftPluginDevelopmentPlugin.LOGGER, locator).remapJar(
                new File(mavenOutput.getParentFile(), "mcpatched-temp.jar"), mavenOutput);
        Files.writeString(versionFile, extension.build);
        Files.writeString(new File(mavenOutput.getParentFile(),
                        "mcpatched-" + extension.localVersion + "-" + extension.build + ".pom").toPath(),
                MavenPom.MAVEN_POM.replace("%s", extension.localVersion + "-" + extension.build));
    }
}
