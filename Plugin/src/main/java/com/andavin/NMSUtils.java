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

import com.andavin.nbt.wrapper.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static com.andavin.MinecraftVersion.CURRENT_SERVER_VERSION;
import static com.andavin.reflect.Reflection.findClass;
import static com.andavin.reflect.Reflection.newInstance;

public final class NMSUtils extends JavaPlugin {

    private static final String PACKAGE = NMSUtils.class.getPackage().getName();
    private static final String VERSION_PREFIX = PACKAGE + '.' + CURRENT_SERVER_VERSION;

    private static NMSUtils instance;
    private static boolean fastAsyncSupport;

    public NMSUtils() {
        instance = this;
        NBTHelper.register(
                NBTTagEnd.class,
                NBTTagByte.class,
                NBTTagShort.class,
                NBTTagInt.class,
                NBTTagLong.class,
                NBTTagFloat.class,
                NBTTagDouble.class,
                NBTTagByteArray.class,
                NBTTagIntArray.class,
                NBTTagLongArray.class,
                NBTTagCompound.class,
                NBTTagList.class,
                NBTTagString.class
        );
    }

    @Override
    public void onEnable() {
        fastAsyncSupport = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
    }

    /**
     * The singleton instance of the {@link NMSUtils} plugin.
     *
     * @return This plugin instance.
     */
    public static NMSUtils getInstance() {
        return instance;
    }

    /**
     * Tell if FastAsyncWorldEdit is available for use.
     *
     * @return If FastAsyncWorldEdit is supported.
     */
    public static boolean fastAsyncSupport() {
        return fastAsyncSupport;
    }

    /**
     * Get an instance of an NMS Utils versioned class.
     *
     * @param clazz The class to get the versioned counterpart for.
     * @param args The arguments to pass to the constructor (non can be null).
     * @param <T> The type of class to retrieve.
     * @return The instance of the versioned type.
     * @throws UnsupportedOperationException If the class is not found (no supported).
     */
    public static <T extends Versioned> T getVersionedInstance(Class<T> clazz, Object... args) throws UnsupportedOperationException {

        String name = clazz.getName().substring(PACKAGE.length());
        Class<T> found = findClass(VERSION_PREFIX + name);
        if (found == null) {
            throw new UnsupportedOperationException("Class " + clazz +
                    " is not currently supported for version " + CURRENT_SERVER_VERSION);
        }

        return newInstance(found, args);
    }
}
