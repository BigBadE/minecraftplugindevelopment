package com.bigbade.minecraftplugindevelopment.common;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

@RequiredArgsConstructor
public class FileRemapper {
    private final Logger logger;
    private final FileLocator fileLocator;

    public void remapJar(File jar, File output) throws IOException {
        AsmRemapper remapper = new AsmRemapper();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                fileLocator.getMappingFile("bukkit-1.17.1-cl.csrg").openStream()))) {
            MappingParser.getMappingParser(AsmRemapper.InputType.SPIGOT, remapper, 1).parse(reader);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                fileLocator.getMappingFile("bukkit-1.17.1-members.csrg").openStream()))) {
            MappingParser.getMappingParser(AsmRemapper.InputType.SPIGOT, remapper, 0).parse(reader);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new URL(fileLocator.getVanillaMappingsFile()).openStream()))) {
            MappingParser.getMappingParser(AsmRemapper.InputType.NOTCHIAN, remapper, 0).parse(reader);
        }

        remapJar(remapper.build());
    }

    public static byte[] readFully(InputStream in) throws IOException {
        return readFully(in, true);
    }

    public static byte[] readFully(InputStream in, boolean close) throws IOException {
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
            if(close) {
                in.close();
            }
        }
    }

    private static void remapJar(Remapper remapper) throws IOException {
        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream("testing/mcpatched-temp.jar"));
             JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream("testing/mcpatched-remapped.jar"))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (!entry.getName().endsWith(".class")) {
                    jarOutputStream.putNextEntry(entry);
                    jarOutputStream.write(readFully(jarInputStream, false));
                    jarOutputStream.closeEntry();
                    continue;
                }

                String fileName = entry.getName().substring(0, entry.getName().length() - 6);

                //Spigot/Paper leave in a Vanilla util anonymous inner class for no reason, ignore it.
                if(fileName.equals("net/minecraft/Util$2")) continue;

                String found = remapper.map(fileName);
                if(found != null) {
                    fileName = found;
                }

                jarOutputStream.putNextEntry(new JarEntry(fileName + ".class"));

                ClassReader classReader = new ClassReader(jarInputStream);
                ClassWriter classWriter = new ClassWriter(classReader, 0);
                ClassVisitor classVisitor = new ClassRemapper(classWriter, remapper);
                classReader.accept(classVisitor, 0);
                jarOutputStream.write(classWriter.toByteArray());
                jarOutputStream.closeEntry();
            }
        }
    }
}
