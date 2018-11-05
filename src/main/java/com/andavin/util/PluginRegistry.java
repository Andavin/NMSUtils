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

package com.andavin.util;

import com.andavin.NMSUtils;
import com.andavin.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Andavin
 * @since May 16, 2018
 */
public final class PluginRegistry {

    private static final Map<String, WeakReference<Plugin>> PLUGINS = new HashMap<>();

    static {
        // A least one plugin should be loaded at the point
        // of a reference to this class therefore we should
        // be able to get a default here
        refresh();
    }

    /**
     * Register a plugin with its package reference
     * in order to use plugin referencing utils such
     * as {@link com.andavin.util.Logger} or {@link Scheduler}.
     * <p>
     * Note that it is expected that the plugin class is
     * in the top level package of the project.
     *
     * @param plugin The {@link Plugin plugin} instance to register.
     */
    public static void register(Plugin plugin) {

        Class<? extends Plugin> clazz = plugin.getClass();
        WeakReference<Plugin> reference = new WeakReference<>(plugin);
        String name = clazz.getName();
        PLUGINS.put(name, reference);
        int first = name.indexOf('.');
        if (first == -1) {
            return;
        }

        for (int dot = name.lastIndexOf('.'); first < dot; dot = name.lastIndexOf('.')) {
            PLUGINS.put(name = name.substring(0, dot), reference);
        }
    }

    /**
     * Get the {@link PluginLogger logger} by a class that called
     * the class that called this.
     *
     * @return The {@link Logger} for the class or {@link Bukkit#getLogger()}
     *         if none was registered to the class name.
     */
    @Nonnull
    static Logger getLogger() {

        Plugin plugin = getPlugin(Reflection.getCallerClass(1));
        for (int tries = 2; plugin == null && tries <= 10; tries++) {
            plugin = getPlugin(Reflection.getCallerClass(tries));
        }

        return plugin != null ? plugin.getLogger() : Bukkit.getLogger();
    }

    /**
     * Get the {@link Plugin} that called the method calling this
     * method. This will return the default {@link NMSUtils plugin}
     * if no other plugin can be found within the attempts allotted.
     *
     * @param attempts The attempts to (calls to go back) to try to
     *                 find the plugin within.
     * @return The plugin that called the method.
     */
    @Nonnull
    public static Plugin getPlugin(int attempts) {

        String className = Reflection.getCallerClass(1); // Exclude the class that called this
        Plugin plugin = getPlugin(className);
        for (int tries = 2; plugin == null && tries <= attempts; tries++) {
            plugin = getPlugin(Reflection.getCallerClass(tries));
        }

        return plugin != null ? plugin : NMSUtils.getInstance();
    }

    /**
     * Get the {@link Plugin} by a class name that is registered.
     *
     * @param className The name of the class to get the plugin for.
     * @return The {@link Plugin} that is registered with the class name.
     */
    @Nullable
    public static Plugin getPlugin(String className) {

        char[] chars = className.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {

            if (chars[i] == '.') {

                WeakReference<Plugin> reference = PLUGINS.get(new String(chars, 0, i));
                if (reference != null && reference.get() != null) {
                    return reference.get();
                }
            }
        }

        refresh();
        return null;
    }

    private static void refresh() {

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            register(plugin);
        }
    }
}
