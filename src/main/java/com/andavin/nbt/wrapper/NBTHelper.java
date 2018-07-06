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

package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A basic helper class to store wrapped and wrapping class
 * for NBT and also useful methods to wrap NMS objects or create
 * new ones from scratch.
 *
 * @author Andavin
 * @since May 12, 2018
 */
public final class NBTHelper {

    private static final Method READ, WRITE;
    private static final Map<Byte, Constructor<? extends NBTBase>> TYPE_IDS = new HashMap<>();
    private static final Map<Class<? extends NBTBase>, Constructor<?>> WRAPPED = new HashMap<>();
    private static final Map<Class<?>, Constructor<? extends NBTBase>> WRAPPERS = new HashMap<>();

    static {
        final Class<?> compoundTag = Reflection.getMcClass("NBTTagCompound");
        final Class<?> streamTools = Reflection.getMcClass("NBTCompressedStreamTools");
        READ = Reflection.getMethod(streamTools, "a", InputStream.class);
        WRITE = Reflection.getMethod(streamTools, "a", compoundTag, OutputStream.class);
    }

    /**
     * Register NBT wrapper classes (extend {@link NBTBase}).
     *
     * @param classes The classes to register.
     */
    @SafeVarargs
    public static void register(final Class<? extends NBTBase>... classes) {

        for (final Class<? extends NBTBase> clazz : classes) {

            final String name = clazz.getSimpleName();
            final Class<?> nmsType = Reflection.getMcClass(name);
            final NBTTag type = clazz.getAnnotation(NBTTag.class);
            if (type != null && nmsType != null) {
                ConfigurationSerialization.registerClass(clazz);
                final Constructor<?> wrap = Reflection.getConstructor(nmsType, type.params());
                final Constructor<? extends NBTBase> wrapper = Reflection.getConstructor(clazz, Object.class);
                checkState(wrapper != null, "%s missing constructor", name);
                checkState(wrap != null, "%s incorrect annotation types", name);
                WRAPPERS.put(nmsType, wrapper);
                TYPE_IDS.put(type.typeId(), wrapper);
                WRAPPED.put(clazz, wrap);
            }
        }
    }

    /**
     * Cast the given tag to the type that is represented by the
     * given type ID.
     *
     * @param typeId The {@link NBTType type ID}.
     * @param tag The {@link NBTBase tag} to cast.
     * @param <T> The type that corresponds to the type ID.
     * @return The object casted to the given ID type.
     * @throws ClassCastException If the type ID given is not the type of
     *                            the tag or {@code T} is not the same type
     *                            ID as the passed in type ID.
     * @see NBTType NBT Type IDs
     */
    public static <T extends NBTBase> T cast(final byte typeId, final NBTBase tag) {

        final Constructor<? extends NBTBase> con = TYPE_IDS.get(typeId);
        supportCheck(con, typeId);
        final Class<? extends NBTBase> clazz = con.getDeclaringClass();
        if (!clazz.isInstance(tag)) {
            throw new ClassCastException("Cannot cast " + tag.getClass().getSimpleName() + " (" + tag.getTypeId() +
                                         ") to " + clazz.getSimpleName() + " (" + typeId + ')');
        }

        return (T) clazz.cast(tag);
    }

    /**
     * Wrap an NMS NBT object into its corresponding wrapper class
     * that extends {@link NBTBase}.
     *
     * @param nbt The raw NMS NBT object to wrap (extends NMS {@code NBTBase}).
     * @param <T> The {@link NBTBase type} to create and return.
     * @return The newly created wrapper for the given NBT object.
     * @throws ClassCastException If the T is not the correct wrapper type
     *                            for the passed in object.
     * @throws NullPointerException If the NBT object is {@code null}.
     */
    public static <T extends NBTBase> T wrap(final Object nbt) {
        checkNotNull(nbt, "NBT object cannot be null");
        final Constructor<? extends NBTBase> wrapper = WRAPPERS.get(nbt.getClass());
        supportCheck(wrapper, nbt.getClass().getSimpleName());
        return (T) Reflection.getInstance(wrapper, nbt);
    }

    /**
     * Wrap an NMS NBT object into its corresponding wrapper class
     * that extends {@link NBTBase}, but retrieve the wrapper class
     * by the passed in type ID which should be the type ID of the
     * passed in object.
     *
     * @param typeId The {@link NBTType type ID} of the object to wrap.
     * @param nbt The raw NMS NBT object to wrap (extends NMS {@code NBTBase}).
     * @param <T> The {@link NBTBase type} to create and return.
     * @return The newly created wrapper for the given NBT object.
     * @throws ClassCastException If the T is not the correct wrapper type
     *                            for the NBT type ID passed in.
     * @throws NullPointerException If the NBT object is {@code null}.
     * @see NBTType NBT Type IDs
     */
    public static <T extends NBTBase> T wrap(final byte typeId, final Object nbt) {
        checkNotNull(nbt, "NBT object cannot be null");
        final Constructor<? extends NBTBase> con = TYPE_IDS.get(typeId);
        supportCheck(con, nbt.getClass().getSimpleName());
        return (T) Reflection.getInstance(con, nbt);
    }

    /**
     * Create a new NMS NBT tag object from the wrapper class.
     *
     * @param clazz The wrapper class (extends {@link NBTBase})
     *              to create the tag for.
     * @param args The arguments to pass to the NMS constructor.
     * @return The newly create NMS NBT tag object.
     */
    public static Object createTag(final Class<? extends NBTBase> clazz, final Object... args) {
        final Constructor<?> con = WRAPPED.get(clazz);
        supportCheck(con, clazz.getSimpleName());
        return Reflection.getInstance(con, args);
    }

    /**
     * Read the given {@link File} into an {@link NBTTagCompound}.
     * The file must be an NBT file ending in {@code .dat}.
     *
     * @param file The file to read the NBT data from.
     * @return The {@link NBTTagCompound} read from the file.
     * @throws IOException If something goes wrong while reading the file.
     * @throws IllegalArgumentException If the file is not an NBT file ending in {@code .dat}.
     */
    public static NBTTagCompound read(final File file) throws IOException, IllegalArgumentException {
        checkArgument(file.getPath().endsWith(".dat"), "can only read NBT files with the extension 'dat'.");
        final InputStream stream = new BufferedInputStream(new FileInputStream(file));
        return Reflection.invokeMethod(READ, null, stream);
    }

    /**
     * Write the given {@link NBTTagCompound} to the given {@link File}.
     * The file must be an NBT file ending in {@code .dat}.
     *
     * @param file The file to write the NBT data to.
     * @param tag The {@link NBTTagCompound} to write to the file.
     * @throws IOException If something goes wrong while writing to the file.
     * @throws IllegalArgumentException If the file is not an NBT file ending in {@code .dat}.
     */
    public static void write(final File file, final NBTTagCompound tag) throws IOException, IllegalArgumentException {
        checkArgument(file.getPath().endsWith(".dat"), "can only read NBT files with the extension 'dat'.");
        final OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
        Reflection.invokeMethod(WRITE, null, tag.getWrapped(), stream);
    }

    private static void supportCheck(final Constructor<?> con, final Object support) {

        if (con == null) {
            throw new UnsupportedOperationException(support + " is not supported on this server version.");
        }
    }
}
