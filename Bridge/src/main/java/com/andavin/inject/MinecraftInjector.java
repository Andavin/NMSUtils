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

import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
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

    private static final Set<String> EXCLUDED = new HashSet<>(); // Excluded from the new JAR
    private static final Map<String, Injector> INJECTORS = new HashMap<>();
    private static final Map<Class<?>, Plugin> INJECTION_CLASSES = new LinkedHashMap<>();
    private static final List<ByteClassInjector> BYTE_INJECTION_CLASSES = new ArrayList<>();

    private static final String INJECTOR_VERSION_DESC, INJECTOR_VERSION_CLASS;

    static {
        String internalName = Type.getInternalName(InjectorVersion.class);
        INJECTOR_VERSION_DESC = 'L' + internalName + ';';
        INJECTOR_VERSION_CLASS = internalName + ".class";
    }

    /**
     * Register an {@link Injector} to inject into a Bukkit or
     * Minecraft class.
     *
     * @param classToAlter The class to inject and alter.
     * @param injector The injector to do the alterations.
     */
    public static void register(Class<?> classToAlter, Injector injector) {
        INJECTORS.put(Type.getInternalName(classToAlter) + ".class", injector);
    }

    /**
     * Inject the given class into the Minecraft JAR so that
     * it will be loaded on server startup and can be recognized
     * by injected code.
     * <p>
     * The given class must be annotated with the {@link InjectorVersion}
     * annotation to signify its version. If the version is the same
     * as the class found in the JAR, then it will not be replaced.
     * <p>
     * Note that the given class must be found within the same JAR as
     * the {@link Plugin} or it will not be able to be updated in the
     * Minecraft JAR.
     *
     * @param plugin The plugin that the class belongs to.
     * @param clazz The class to inject into the JAR.
     */
    public static void injectClass(Plugin plugin, Class<?> clazz) {

        if (clazz.getDeclaredAnnotation(InjectorVersion.class) != null) {
            INJECTION_CLASSES.put(clazz, plugin);
            return;
        }

        throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with InjectorVersion.");
    }

    /**
     * Inject the given class into the Minecraft JAR so that
     * it will be loaded on server startup and can be recognized
     * by injected code.
     * <p>
     * The given class must be annotated with the {@link InjectorVersion}
     * annotation to signify its version. If the version is the same
     * as the class found in the JAR, then it will not be replaced.
     * <p>
     * It is recommended to avoid using this method unless absolutely
     * necessary. Instead it is preferred to create a function class
     * via the Java compiler and use the {@link #injectClass(Plugin, Class)}
     * method instead to ensure class structure and validity.
     *
     * @param injector The {@link ByteClassInjector} to use to inject the byte class.
     * @see #injectClass(Plugin, Class)
     */
    public static void injectClass(ByteClassInjector injector) {
        BYTE_INJECTION_CLASSES.add(injector);
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

            boolean annotation = false;
            Enumeration<JarEntry> entries = jar.entries();
            List<JarEntry> unchanged = new ArrayList<>(jar.size());
            Map<String, ClassWriter> changed = new HashMap<>((int) (INJECTORS.size() / 0.75));
            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {

                    if (name.startsWith("org/bukkit") || name.startsWith("net/minecraft")) {

                        Injector injector = INJECTORS.get(name);
                        if (injector != null) {

                            Object writer = inject(jar, entry, injector);
                            if (writer instanceof ClassWriter) {
                                Logger.debug("Class {} has been found and altered successfully.", name);
                                changed.put(entry.getName(), (ClassWriter) writer);
                            } else {
                                unchanged.add(entry);
                            }

                            continue;
                        }
                    } else if (!annotation && name.equals(INJECTOR_VERSION_CLASS)) {
                        annotation = true;
                    }
                }

                unchanged.add(entry);
            }

            if (changed.isEmpty() && INJECTION_CLASSES.isEmpty()) {
                Logger.info("Nothing found to inject.");
                INJECTORS.clear(); // No memory leaks
                return false;
            }

            Logger.info("Found {} classes to alter and {} new classes that need to be injected.",
                    changed.size(), INJECTION_CLASSES.size() + BYTE_INJECTION_CLASSES.size());

            Path jarPath = jarFile.toPath();
            String parent = jarFile.getParent();
            File tempJar = new File(parent, "injection-temp.jar");
            try (JarOutputStream output = new JarOutputStream(new FileOutputStream(tempJar))) {

                if (!annotation) { // Add the annotation to the JAR

                    try {

                        String path = InjectorVersion.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                        try (JarFile annotationJar = new JarFile(new File(path))) {

                            Enumeration<JarEntry> annEntries = annotationJar.entries();
                            while (annEntries.hasMoreElements()) {

                                JarEntry entry = annEntries.nextElement();
                                if (entry.getName().equals(INJECTOR_VERSION_CLASS)) {
                                    writeClass(annotationJar, output, entry);
                                    break;
                                }
                            }
                        }

                    } catch (URISyntaxException e) {
                        Logger.severe(e);
                    }
                }

                // If there are classes that need injected, then attempt to write them.
                boolean classesUnwritten = !INJECTION_CLASSES.isEmpty() && writeClasses(output);
                if (classesUnwritten && changed.isEmpty()) {
                    // If they do not need to be written and there are no other changes
                    // then cleanup the file and return that there were no changes
                    Logger.info("No new class versions were available. Nothing to inject.");
                    tempJar.delete();
                    return false;
                }

                String backupName = jarName.substring(0, jarIndex) + "-backup.jar";
                File backup = new File(parent, backupName);
                if (!backup.exists()) {
                    Logger.info("Making a backup of the current server JAR as {}.", backupName);
                    Logger.info("Restore to the backup at any time if there are issues.");
                    Files.copy(jarPath, backup.toPath());
                }

                Logger.info("Injecting all alterations into a temporary JAR...");
                for (JarEntry entry : unchanged) {

                    String name = entry.getName();
                    int classIndex = name.indexOf(".class");
                    if (classIndex != -1) { // Remove the classes that need to be added

                        String className = name.substring(0, classIndex);
                        if (EXCLUDED.contains(className)) {
                            Logger.debug("Removing old version of class {}.", name);
                            continue;
                        }

                        int subIndex = name.lastIndexOf('$');
                        if (subIndex != -1 && EXCLUDED.contains(name.substring(0, subIndex))) {
                            Logger.debug("Removing old version of class {}.", name);
                            continue;
                        }
                    }

                    writeClass(jar, output, entry);
                }

                for (Entry<String, ClassWriter> entry : changed.entrySet()) {
                    output.putNextEntry(new JarEntry(entry.getKey()));
                    output.write(entry.getValue().toByteArray());
                    output.flush();
                    output.closeEntry();
                }
            }

            Logger.info("Replacing server JAR ({}) with the injected version.", jarName);
            Files.copy(tempJar.toPath(), jarPath, StandardCopyOption.REPLACE_EXISTING);
            Logger.info("Deleting temporary injected JAR.");
            tempJar.delete();
            Logger.info("Injection completed successfully. Please restart the server.");
            Bukkit.shutdown();

        } catch (IOException e) {
            Logger.severe(e, "Failed to inject JAR. Please inform the developer.");
        }

        return true;
    }

    private static Object inject(JarFile jar, JarEntry entry, Injector injector) {

        try (InputStream stream = jar.getInputStream(entry)) {

            ClassReader reader = new ClassReader(stream);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_FRAMES);
            if (injector.inject(node)) {
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
                node.accept(writer);
                return writer;
            }

            return entry;
        } catch (Exception e) {
            Logger.severe(e, "Exception thrown during injection for injector {}", injector.getClass().getName());
            return entry; // No change
        }
    }

    /**
     * Attempt to write all of the injected classes to the new
     * JAR checking their versions against any matching classes
     * that currently reside in the current JAR and write the
     * correct class data.
     * <p>
     * Note that this method will only write to the JAR if the
     * version is changed and otherwise will leave it up to the
     * unchanged class writer to transfer the old class.
     *
     * @param output The {@link JarOutputStream} to write the classes to.
     * @return True if there were no classes that needed to be written at this time.
     * @throws IOException If anything goes wrong while writing to the JAR.
     */
    private static boolean writeClasses(JarOutputStream output) throws IOException {

        JarFile jar = null;
        URL location = null; // The current location for each class
        Plugin plugin = null;
        boolean unchanged = true; // Default to no changes
        nextInjectionEntry:
        for (Entry<Class<?>, Plugin> classEntry : INJECTION_CLASSES.entrySet()) {

            Class<?> clazz = classEntry.getKey();
            // Get the current version in the Minecraft JAR
            String version = clazz.getDeclaredAnnotation(InjectorVersion.class).value();
            if (jar == null || classEntry.getValue() != plugin) {

                plugin = classEntry.getValue();
                location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
                try {
                    jar = new JarFile(new File(location.toURI().getPath()));
                } catch (URISyntaxException e) {
                    Logger.severe(e);
                    continue;
                }
            }

            boolean parentWritten = false;
            List<JarEntry> subClasses = null;
            String internalName = Type.getInternalName(clazz);
            String clazzName = internalName + ".class";
            String subClassPrefix = internalName + '$';
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {

                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                boolean subClass = name.startsWith(subClassPrefix);
                if (subClass && !parentWritten) {

                    if (subClasses == null) {
                        subClasses = new ArrayList<>(1);
                    }

                    subClasses.add(entry);
                    continue;
                }

                if (subClass || name.equals(clazzName)) { // Write the class and all subclasses

                    // Check that they are in a different JAR (one in plugin the other in server)
                    // If they are in the same JAR (i.e. in the plugin) just write it immediately
                    // since it's not yet present in the server JAR
                    if (subClass || clazz.getProtectionDomain().getCodeSource().getLocation() == location) {

                        if (!subClass) {

                            Logger.debug("Doing initial write of class {}.", name);
                            parentWritten = true;
                            writeClass(jar, output, entry);
                            if (subClasses != null) {

                                for (JarEntry subEntry : subClasses) { // Write all previous subclasses
                                    writeClass(jar, output, subEntry);
                                }
                            }
                        } else {
                            writeClass(jar, output, entry);
                        }

                    } else {

                        try (InputStream input = jar.getInputStream(entry)) {

                            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                            writeClass(input, byteStream); // Write the class to the byte stream

                            ClassReader reader = new ClassReader(byteStream.toByteArray()); // Read the entry
                            ClassNode node = new ClassNode();
                            reader.accept(node, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                            if (node.visibleAnnotations != null) {

                                for (AnnotationNode annotationNode : node.visibleAnnotations) {

                                    // Get the version annotation from the class
                                    if (annotationNode.desc.equals(INJECTOR_VERSION_DESC)) {

                                        List<Object> values = annotationNode.values; // Check that the version is the same
                                        if (values != null && values.size() >= 2 && values.get(1).equals(version)) {
                                            continue nextInjectionEntry; // This entry doesn't need a change
                                        }

                                        Logger.info("Found newer version of class {}.", name);
                                        break;
                                    }
                                }
                            }

                            EXCLUDED.add(internalName); // Exclude so the old version will not be written
                            parentWritten = true;
                            // Write the entry to the new JAR to override the previous version
                            output.putNextEntry(new JarEntry(name));
                            output.write(byteStream.toByteArray()); // Write a new copy
                            output.flush();
                            output.closeEntry();

                            if (subClasses != null) {

                                for (JarEntry subEntry : subClasses) {
                                    writeClass(jar, output, subEntry);
                                }
                            }
                        }
                    }

                    unchanged = false;
                }
            }
        }

        for (ByteClassInjector injector : BYTE_INJECTION_CLASSES) {

            String name = injector.getInternalName();
            String version = injector.getVersion();
            try {
                // Get the current class wherever it is - since there's only one version
                // of the compiled class (unlike above) we don't have to worry about mismatches
                Class<?> clazz = Class.forName(name.replace('/', '.'));
                InjectorVersion annotation = clazz.getDeclaredAnnotation(InjectorVersion.class);
                if (annotation != null && annotation.value().equals(version)) {
                    continue;
                }

                Logger.info("Found newer version of class {}.", name);
            } catch (ClassNotFoundException e) {
                // If the class does not exist then default to this version
                Logger.debug("Doing initial write of class {}.", name);
            }

            // Exclude so the old version will not be written
            EXCLUDED.add(name);
            // Write the entry to the new JAR to override the previous version
            output.putNextEntry(new JarEntry(name + ".class"));
            output.write(injector.dump());
            output.flush();
            output.closeEntry();
            unchanged = false;
        }

        return unchanged;
    }

    private static void writeClass(JarFile jar, JarOutputStream output, JarEntry entry) throws IOException {

        try (InputStream input = jar.getInputStream(entry)) {
            output.putNextEntry(new JarEntry(entry.getName()));
            writeClass(input, output);
            output.closeEntry();
        }
    }

    private static void writeClass(InputStream input, OutputStream output) throws IOException {

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }

        output.flush();
    }
}
