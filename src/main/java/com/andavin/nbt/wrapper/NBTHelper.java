package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

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
@SuppressWarnings("unchecked")
public final class NBTHelper {

    private static final Map<Byte, Constructor<? extends NBTBase>> TYPE_IDS = new HashMap<>();
    private static final Map<Class<? extends NBTBase>, Constructor<?>> WRAPPED = new HashMap<>();
    private static final Map<Class<?>, Constructor<? extends NBTBase>> WRAPPERS = new HashMap<>();

    /**
     * Register NBT wrapper classes (extend {@link NBTBase}).
     *
     * @param classes The classes to register.
     */
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
        supportCheck(con, String.valueOf(typeId));
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

    private static void supportCheck(final Constructor<?> con, final String support) {

        if (con == null) {
            throw new UnsupportedOperationException(support + " is not supported on this server version.");
        }
    }
}
