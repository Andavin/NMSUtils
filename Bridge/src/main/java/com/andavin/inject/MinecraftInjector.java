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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
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
public final class MinecraftInjector {

    private static final Map<String, Injector> INJECTIONS = new HashMap<>();

    static {
        INJECTIONS.put(MinecraftServerInjector.CLASS_NAME, Versioned.getInstance(MinecraftServerInjector.class));
    }

    private MinecraftInjector() {
    }

    /**
     * Inject all of the altered code and carry out those
     * alterations if needed. If this method succeeds and
     * there was code that needed to be injected, then the
     * server will immediately shutdown after this.
     *
     * @return If the injection alterations occurred and the
     *         server will be shutting down.
     */
    public static boolean inject() {

        String pathToJar;
        try {
            pathToJar = Bukkit.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            Logger.severe(e, "Could not locate server JAR. Please inform the developer.");
            return false;
        }

        File jarFile = new File(pathToJar);
        String jarName = jarFile.getName();
        int jarIndex = jarName.indexOf(".jar");
        if (jarIndex == -1) {
            throw new IllegalStateException("The server jar found is not a JAR: " + jarName);
        }

        try (JarFile jar = new JarFile(jarFile)) {

            Enumeration<JarEntry> entries = jar.entries();
            List<Class<?>> classesToAdd = new LinkedList<>();
            List<JarEntry> unchanged = new ArrayList<>(jar.size());
            Map<String, ClassWriter> changed = new HashMap<>((int) (INJECTIONS.size() / 0.75));
            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.endsWith(".class") &&
                        (name.startsWith("org/bukkit") || name.startsWith("net/minecraft"))) {

                    Injector injector = INJECTIONS.get(name);
                    if (injector != null) {

                        Object writer = inject(jar, entry, injector, classesToAdd);
                        if (writer instanceof ClassWriter) {
                            changed.put(entry.getName(), (ClassWriter) writer);
                        } else {
                            unchanged.add(entry);
                        }

                        continue;
                    }
                }

                unchanged.add(entry);
            }

            if (changed.isEmpty() && classesToAdd.isEmpty()) {
                Logger.info("Nothing found to inject.");
                return false;
            }

            Logger.info("Found {} classes to alter, {} new classes that need to be injected and {} files left unchanged.",
                    changed.size(), classesToAdd.size(), unchanged.size());

            Path jarPath = jarFile.toPath();
            String parent = jarFile.getParent();
            String backupName = jarName.substring(0, jarIndex) + "-backup.jar";
            File backup = new File(parent, backupName);
            if (!backup.exists()) {
                Logger.info("Making a backup of the current server JAR as {}.", backupName);
                Logger.info("Restore to the backup at any time if there are issues.");
                Files.copy(jarPath, backup.toPath());
            }

            List<String> classNames = new ArrayList<>(classesToAdd.size() * 2);
            for (Class<?> clazz : classesToAdd) {
                String internalName = Type.getInternalName(clazz);
                String clazzName = internalName + ".class";
                String subClassPrefix = internalName + '$';
                classNames.add(clazzName);
                classNames.add(subClassPrefix);
            }

            Logger.info("Creating temporary injection JAR.");
            File tempJar = new File(parent, "injection-temp.jar");
            Logger.info("Injecting all alterations into the temporary JAR...");
            try (JarOutputStream output = new JarOutputStream(new FileOutputStream(tempJar))) {

                entries:
                for (JarEntry entry : unchanged) {

                    String name = entry.getName();
                    for (String className : classNames) {
                        // Remove the classes that need to be added
                        if (name.startsWith(className)) {
                            continue entries;
                        }
                    }

                    output.putNextEntry(new JarEntry(name));
                    try (InputStream input = jar.getInputStream(entry)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }

                    output.flush();
                    output.closeEntry();
                }

                for (Entry<String, ClassWriter> entry : changed.entrySet()) {
                    output.putNextEntry(new JarEntry(entry.getKey()));
                    output.write(entry.getValue().toByteArray());
                    output.flush();
                    output.closeEntry();
                }

                URL lastLocation = null;
                JarFile otherJar = null;
                for (Class<?> clazz : classesToAdd) {

                    URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
                    if (otherJar == null || lastLocation != location) {

                        lastLocation = location;
                        try {
                            otherJar = new JarFile(new File(location.toURI().getPath()));
                        } catch (URISyntaxException e) {
                            Logger.severe(e);
                            continue;
                        }
                    }

                    String internalName = Type.getInternalName(clazz);
                    String clazzName = internalName + ".class";
                    String subClassPrefix = internalName + '$';
                    Enumeration<JarEntry> otherEntries = otherJar.entries();
                    while (otherEntries.hasMoreElements()) {

                        JarEntry entry = otherEntries.nextElement();
                        String name = entry.getName();
                        if (name.equals(clazzName) || name.startsWith(subClassPrefix)) { // Write the class and all subclasses

                            output.putNextEntry(new JarEntry(name));
                            try (InputStream input = otherJar.getInputStream(entry)) {

                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = input.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                            }

                            output.flush();
                            output.closeEntry();
                        }
                    }
                }
            }

            Logger.info("Replacing server JAR ({}) with the injected version.", jarName);
            Files.copy(tempJar.toPath(), jarPath, StandardCopyOption.REPLACE_EXISTING);
            Logger.info("Deleting temporary injected JAR.");
            tempJar.delete();
            Logger.info("Injection completed successfully. Stopping server.");
            Bukkit.shutdown();

        } catch (IOException e) {
            Logger.severe(e, "Failed to inject JAR. Please inform the developer.");
        }

        return true;
    }

    private static Object inject(JarFile jar, JarEntry entry, Injector injector, List<Class<?>> classesToAdd) throws IOException {

        try (InputStream stream = jar.getInputStream(entry)) {
            ClassReader reader = new ClassReader(stream);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_FRAMES);
            ClassWriter writer = injector.inject(node, reader, classesToAdd);
            return writer != null ? writer : entry;
        }
    }
}
