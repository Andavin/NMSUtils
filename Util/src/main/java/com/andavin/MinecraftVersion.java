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

package com.andavin;

import com.andavin.reflect.Reflection;
import org.bukkit.Bukkit;

/**
 * An enum constant that represents the possibilities of each
 * version of Minecraft to date since Minecraft {@code 1.7.10}.
 * <p>
 * Each version is representative of the latest minor version
 * from within the major version. For example, {@link #v1_9_R1}
 * is the version {@code 1.9.4} since that was the last version
 * to be released for version {@code 1.9}.
 * <p>
 * All fields within this class may be statically imported as
 * they are each named in a descriptive manner.
 * <p>
 * Methods provided in this class are shortcut methods that provide
 * easy use of the {@link #compareTo(Enum)} method. They should not
 * be statically imported, but instead used alongside a statically
 * imported field:
 * <pre>
 *     import static com.andavin.MinecraftVersion.v1_12_R1;
 *
 *     MinecraftVersion.is(v1_12_R1); // If this version is 1.12.2
 *     MinecraftVersion.lessThan(v1_12_R1); // If this version is 1.11.2 or earlier
 *     MinecraftVersion.greaterThan(v1_12_R1); // If this later than 1.12.2
 *     MinecraftVersion.greaterThanOrEqual(v1_12_R1); // If this version is 1.12.2 or later
 * </pre>
 *
 * @since November 12, 2018
 * @author Andavin
 * @see Reflection
 * @see Reflection#findMcClass(String)
 * @see Reflection#findCraftClass(String)
 */
public enum MinecraftVersion {

    /**
     * The representation of the Minecraft version {@code 1.7.10}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_7_R4,

    /**
     * The representation of the Minecraft version {@code 1.8.8}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_8_R3,

    /**
     * The representation of the Minecraft version {@code 1.9.4}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_9_R1,

    /**
     * The representation of the Minecraft version {@code 1.10.2}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_10_R1,

    /**
     * The representation of the Minecraft version {@code 1.11.2}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_11_R1,

    /**
     * The representation of the Minecraft version {@code 1.12.2}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_12_R1,

    /**
     * The representation of the Minecraft version {@code 1.13.2}.
     * The name of this enum is actual version string that is inserted
     * into the package name for Minecraft server and CraftBukkit packaged.
     */
    v1_13_R2;

    /**
     * The current {@link MinecraftVersion} of this server.
     */
    public static final MinecraftVersion CURRENT_SERVER_VERSION;

    static {

        String versionString = Bukkit.getServer().getClass().getPackage().getName()
                .substring("org.bukkit.craftbukkit.".length());
        try {
            CURRENT_SERVER_VERSION = MinecraftVersion.valueOf(versionString);
        } catch (RuntimeException e) {
            throw new UnsupportedOperationException("Version " + versionString + " is not supported.", e);
        }
    }

    /**
     * The package prefix for all Minecraft server (NMS) classes.
     *
     * @see Reflection#findMcClass(String)
     */
    public static final String MINECRAFT_PREFIX = "net.minecraft.server." + CURRENT_SERVER_VERSION + '.';

    /**
     * The package prefix for all CraftBukkit classes.
     *
     * @see Reflection#findCraftClass(String)
     */
    public static final String CRAFTBUKKIT_PREFIX = "org.bukkit.craftbukkit." + CURRENT_SERVER_VERSION + '.';

    /**
     * Tell if the {@link #CURRENT_SERVER_VERSION current server
     * version} is the specified version.
     * <p>
     * For example, if {@code MinecraftVersion.is(v1_8_R3)} is
     * {@code true}, then the current server version is {@code 1.8.8}.
     *
     * @param version The version to test the current version against.
     * @return If the version is the same as the current version.
     */
    public static boolean is(MinecraftVersion version) {
        return CURRENT_SERVER_VERSION == version;
    }

    /**
     * Tell if the specified server version is less than (i.e. older
     * than) the {@link #CURRENT_SERVER_VERSION current server version}.
     * <p>
     * For example, if {@code MinecraftVersion.lessThan(v1_10_R1)} is
     * {@code true}, then the current server version is {@code 1.9.4}
     * or an earlier version.
     *
     * @param version The version to test the current version against.
     * @return If the version is less than the current version.
     */
    public static boolean lessThan(MinecraftVersion version) {
        return CURRENT_SERVER_VERSION.ordinal() < version.ordinal();
    }

    /**
     * Tell if the specified server version is greater than (i.e. newer
     * than) the {@link #CURRENT_SERVER_VERSION current server version}.
     * <p>
     * For example, if {@code MinecraftVersion.greaterThan(v1_8_R3)} is
     * {@code true}, then the current server version is {@code 1.9.4}
     * or a more recent version (usually meaning version compatibility
     * with {@code 1.9.4}).
     *
     * @param version The version to test the current version against.
     * @return If the version is greater than the current version.
     */
    public static boolean greaterThan(MinecraftVersion version) {
        return CURRENT_SERVER_VERSION.ordinal() > version.ordinal();
    }

    /**
     * Tell if the specified server version is greater than or equal to
     * (i.e. newer than or the same as) the {@link #CURRENT_SERVER_VERSION
     * current server version}.
     * <p>
     * For example, if {@code MinecraftVersion.greaterThanOrEqual(v1_8_R3)}
     * is {@code true}, then the current server version is {@code 1.8.8}
     * or a more recent version (usually meaning version compatibility
     * with {@code 1.8.8}).
     *
     * @param version The version to test the current version against.
     * @return If the version is greater than or equal to the current version.
     */
    public static boolean greaterThanOrEqual(MinecraftVersion version) {
        return CURRENT_SERVER_VERSION.ordinal() >= version.ordinal();
    }
}
