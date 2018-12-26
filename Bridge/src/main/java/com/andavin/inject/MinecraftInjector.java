/*
 * MIT License
 *
 * Copyright (c) 2018 Andavin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andavin.inject;

import com.andavin.Versioned;
import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * @since December 25, 2018
 * @author Andavin
 */
public class MinecraftInjector {

    private static final Map<String, Injector> INJECTIONS = new HashMap<>();

    static {
        INJECTIONS.put(NetworkManagerInjector.CLASS_NAME, Versioned.getInstance(NetworkManagerInjector.class));
    }

    public void inject() {

        String pathToJar;
        try {
            pathToJar = Bukkit.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            Logger.severe(e, "Could not locate server JAR. Please inform the developer.");
            return;
        }

        File jarFile = new File(pathToJar);
        String jarName = jarFile.getName();
        int jarIndex = jarName.indexOf(".jar");
        if (jarIndex == -1) {
            throw new IllegalStateException("The server jar found is not a JAR: " + jarName);
        }

        try (JarFile jar = new JarFile(jarFile)) {

            Enumeration<JarEntry> entries = jar.entries();
            List<JarEntry> unchanged = new ArrayList<>(jar.size());
            Map<JarEntry, ClassWriter> changed = new HashMap<>((int) (INJECTIONS.size() / 0.75));
            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() && name.endsWith(".class") &&
                        (name.startsWith("org/bukkit") || name.startsWith("net/minecraft"))) {

                    Injector injector = INJECTIONS.get(name);
                    if (injector != null) {

                        Object writer = inject(jar, entry, injector);
                        if (writer instanceof ClassWriter) {
                            changed.put(entry, (ClassWriter) writer);
                        } else {
                            unchanged.add(entry);
                        }
                    }

                    continue;
                }

                unchanged.add(entry);
            }

            int size = changed.size();
            if (size > 0) {

                Logger.info("Found {} classes to alter. Injecting into {}...", size, jarName);
                String parent = jarFile.getParent();
                Logger.info("Creating temporary injection JAR.");
                File tempJar = new File(parent, "injection-temp.jar");
                try (JarOutputStream output = new JarOutputStream(new FileOutputStream(tempJar))) {

                    for (JarEntry entry : unchanged) {

                        output.putNextEntry(new JarEntry(entry.getName()));
                        InputStream input = jar.getInputStream(entry);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }

                        input.close();
                        output.flush();
                        output.closeEntry();
                    }

                    for (Entry<JarEntry, ClassWriter> entry : changed.entrySet()) {
                        output.putNextEntry(new JarEntry(entry.getKey().getName()));
                        output.write(entry.getValue().toByteArray());
                        output.flush();
                        output.closeEntry();
                    }
                }

                Path jarPath = jarFile.toPath();
                String backupName = jarName.substring(0, jarIndex) + "-backup.jar";
                File backup = new File(parent, backupName);
                if (!backup.exists()) {
                    Logger.info("Making a backup of the current server JAR as {}.", backupName);
                    Logger.info("Restore to the backup at any time if there are issues.");
                    Files.copy(jarPath, backup.toPath());
                }

                Logger.info("Replacing server JAR with the injected version.");
                Files.copy(tempJar.toPath(), jarPath, StandardCopyOption.REPLACE_EXISTING);
                Logger.info("Deleting temporary injected JAR.");
                tempJar.delete();
                Logger.info("Injection completed successfully. Restarting server.");
                Bukkit.shutdown();
            }

        } catch (IOException e) {
            Logger.severe(e, "Failed to inject JAR. Please inform the developer.");
        }
    }

    private Object inject(JarFile jar, JarEntry entry, Injector injector) throws IOException {

        try (InputStream stream = jar.getInputStream(entry)) {
            ClassReader reader = new ClassReader(stream);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_FRAMES);
            ClassWriter writer = injector.inject(node, reader);
            return writer != null ? writer : entry;
        }
    }
}
