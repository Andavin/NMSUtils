package com.andavin.util;

import com.andavin.NMSUtils;
import com.andavin.reflect.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    public static void register(final Plugin plugin) {
        final Class<? extends Plugin> clazz = plugin.getClass();
        final String pack = clazz.getPackage().getName();
        PLUGINS.put(pack, new WeakReference<>(plugin));
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

        final HashSet<String> excluded = new HashSet<>(3);
        excluded.add(com.andavin.util.Logger.class.getName());
        excluded.add(Reflection.class.getName());
        final String className = Reflection.getCallerClass(0, excluded);

        Plugin plugin = getPlugin(className);
        for (int tries = 1; plugin == null && tries <= 10; tries++) {
            plugin = getPlugin(Reflection.getCallerClass(tries, excluded));
        }

        return plugin == null ? Bukkit.getLogger() : plugin.getLogger();
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
    public static Plugin getPlugin(final int attempts) {

        final Set<String> excluded = Collections.singleton(Scheduler.class.getName());
        final String className = Reflection.getCallerClass(1, excluded); // Exclude the class that called this
        Plugin plugin = getPlugin(className);
        for (int tries = 2; plugin == null && tries <= attempts; tries++) {
            plugin = getPlugin(Reflection.getCallerClass(tries, excluded));
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
    public static Plugin getPlugin(final String className) {

        final char[] chars = className.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {

            if (chars[i] == '.') {

                final WeakReference<Plugin> reference = PLUGINS.get(new String(chars, 0, i));
                if (reference != null && reference.get() != null) {
                    return reference.get();
                }
            }
        }

        refresh();
        return null;
    }

    private static void refresh() {

        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            register(plugin);
        }
    }
}
