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

import com.andavin.reflect.exception.UncheckedClassNotFoundException;
import com.andavin.reflect.exception.UncheckedInvocationTargetException;
import com.andavin.util.Logger;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.andavin.reflect.Reflection.*;
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

        Class<?> compoundTag = findMcClass("NBTTagCompound");
        Class<?> streamTools = findMcClass("NBTCompressedStreamTools");
        READ = findMethod(streamTools, "a", InputStream.class);
        WRITE = findMethod(streamTools, "a", compoundTag, OutputStream.class);

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
                NBTTagCompound.class,
                NBTTagList.class,
                NBTTagString.class
        );

        try {
            // Try to register this. If the byte code insertion
            // was successful this will also be successful
            NBTHelper.register(NBTTagLongArray.class);
        } catch (UncheckedClassNotFoundException e) {
            Logger.debug(e, "Registering NBTTagLongArray");
        }
    }

    /**
     * Register NBT wrapper classes (extend {@link NBTBase}).
     *
     * @param classes The classes to register.
     */
    @SafeVarargs
    private static void register(Class<? extends NBTBase>... classes) {

        for (Class<? extends NBTBase> clazz : classes) {

            String name = clazz.getSimpleName();
            NBTTag type = clazz.getDeclaredAnnotation(NBTTag.class);
            checkState(type != null, "type %s is not annotated with NBTTag", name);
            ConfigurationSerialization.registerClass(clazz);

            Class<?> nmsType = findMcClass(name);
            Constructor<?> wrap = findConstructor(nmsType, type.params());
            Constructor<? extends NBTBase> wrapper = findConstructor(clazz, Object.class);
            WRAPPERS.put(nmsType, wrapper);
            TYPE_IDS.put(type.typeId(), wrapper);
            WRAPPED.put(clazz, wrap);
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
    public static <T extends NBTBase> T cast(byte typeId, NBTBase tag) {

        Constructor<? extends NBTBase> con = TYPE_IDS.get(typeId);
        supportCheck(con, typeId);
        Class<? extends NBTBase> clazz = con.getDeclaringClass();
        if (!clazz.isInstance(tag)) {
            throw new ClassCastException("Cannot cast " + tag.getClass().getSimpleName() + " (" + tag.getTypeId() +
                    ") to " + clazz.getSimpleName() + " (" + typeId + ')');
        }

        return (T) tag;
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
    public static <T extends NBTBase> T wrap(Object nbt) {
        checkNotNull(nbt, "NBT object cannot be null");
        Class<?> clazz = nbt.getClass();
        Constructor<? extends NBTBase> wrapper = WRAPPERS.get(clazz);
        supportCheck(wrapper, clazz.getName());
        return (T) newInstance(wrapper, nbt);
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
    public static <T extends NBTBase> T wrap(byte typeId, Object nbt) {
        checkNotNull(nbt, "NBT object cannot be null");
        Constructor<? extends NBTBase> con = TYPE_IDS.get(typeId);
        supportCheck(con, nbt.getClass().getName());
        return (T) newInstance(con, nbt);
    }

    /**
     * Create a new NMS NBT tag object from the wrapper class.
     *
     * @param clazz The wrapper class (extends {@link NBTBase})
     *              to create the tag for.
     * @param args The arguments to pass to the NMS constructor.
     * @return The newly create NMS NBT tag object.
     */
    public static Object createTag(Class<? extends NBTBase> clazz, Object... args) {
        Constructor<?> con = WRAPPED.get(clazz);
        supportCheck(con, clazz.getName());
        return newInstance(con, args);
    }

    /**
     * Read the given {@link File} into an {@link NBTTagCompound}.
     * <p>
     * Behavior is undefined if the given file does not contain
     * data in NBT format.
     *
     * @param file The file to read the NBT data from.
     * @return The {@link NBTTagCompound} read from the file.
     * @throws UncheckedIOException If something goes wrong while
     *                              reading the file.
     */
    public static NBTTagCompound read(File file) throws UncheckedIOException {

        try (InputStream stream = new FileInputStream(file)) {
            return deserialize(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Deserialize an {@link NBTTagCompound} from the data read
     * from the given stream.
     * <p>
     * Behavior is undefined if the given stream does not contain
     * data in NBT format.
     *
     * @param stream The stream to read the data from.
     * @return A newly create NBTTagCompound containing the data
     *         from the stream.
     * @throws UncheckedIOException If something goes wrong while
     *                              reading from the the stream.
     */
    public static NBTTagCompound deserialize(InputStream stream) throws UncheckedIOException {
        return wrap(deserializeNMS(stream));
    }

    /**
     * Deserialize an NMS {@code NBTTagCompound} from the data
     * read from the given stream.
     * <p>
     * Behavior is undefined if the given stream does not contain
     * data in NBT format.
     *
     * @param stream The stream to read the data from.
     * @return A newly create NBTTagCompound containing the data
     *         from the stream.
     * @throws UncheckedIOException If something goes wrong while
     *                              reading from the the stream.
     */
    public static Object deserializeNMS(InputStream stream) throws UncheckedIOException {

        try {
            return invokeMethod(READ, null, stream);
        } catch (UncheckedInvocationTargetException e) {

            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new UncheckedIOException((IOException) cause);
            }

            throw cause instanceof RuntimeException ? (RuntimeException) cause : new RuntimeException(cause);
        }
    }

    /**
     * Write the given {@link NBTTagCompound} to the given {@link File}.
     *
     * @param file The file to write the NBT data to.
     * @param tag The {@link NBTTagCompound} to write to the file.
     * @throws UncheckedIOException If something goes wrong while
     *                              writing to the file.
     */
    public static void write(File file, NBTTagCompound tag) throws UncheckedIOException {

        try (OutputStream stream = new FileOutputStream(file)) {
            serialize(stream, tag.getWrapped());
            stream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serialize the given {@link NBTTagCompound} to the
     * given {@link OutputStream}.
     *
     * @param stream The stream to write to.
     * @param tag The NBTTagCompound to write to the stream.
     * @throws UncheckedIOException If something goes wrong while
     *                              writing to the the stream.
     */
    public static void serialize(OutputStream stream, NBTTagCompound tag) throws UncheckedIOException {
        serialize(stream, tag.getWrapped());
    }

    /**
     * Serialize the given NMS {@code NBTTagCompound} to the
     * given {@link OutputStream}.
     *
     * @param stream The stream to write to.
     * @param tag The NBTTagCompound to write to the stream.
     * @throws UncheckedIOException If something goes wrong while
     *                              writing to the the stream.
     */
    public static void serialize(OutputStream stream, Object tag) throws UncheckedIOException {

        try {
            invokeMethod(WRITE, null, tag, stream);
        } catch (UncheckedInvocationTargetException e) {

            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw new UncheckedIOException((IOException) cause);
            }

            throw cause instanceof RuntimeException ? (RuntimeException) cause : new RuntimeException(cause);
        }
    }

    private static void supportCheck(Constructor<?> con, Object support) {

        if (con == null) {
            throw new UnsupportedOperationException(support + " is not supported on this server version.");
        }
    }
}
