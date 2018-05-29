package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive type {@code long}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.LONG, params = long.class)
public final class NBTTagLong extends NBTNumber {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagLong"), "data");
    private final long data;

    public NBTTagLong(final long data) {
        super(NBTHelper.createTag(NBTTagLong.class, data));
        this.data = data;
    }

    NBTTagLong(final Object wrapped) {
        super(wrapped);
        //noinspection ConstantConditions
        this.data = Reflection.getValue(DATA, wrapped);
    }

    @Override
    public Long getData() {
        return data;
    }

    @Override
    public long asLong() {
        return data;
    }

    @Override
    public int asInt() {
        return (int) data;
    }

    @Override
    public short asShort() {
        return (short) data;
    }

    @Override
    public byte asByte() {
        return (byte) data;
    }

    @Override
    public double asDouble() {
        return data;
    }

    @Override
    public float asFloat() {
        return data;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }

    /**
     * Deserialize the map into a new NBTBase object as
     * specified by {@link ConfigurationSerializable}.
     *
     * @param map The map that was serialized with
     *        {@link ConfigurationSerializable#serialize()}.
     * @return The newly created, deserialized object.
     */
    public static NBTTagLong deserialize(final Map<String, Object> map) {
        return new NBTTagLong(((Long) map.get("data")).longValue());
    }
}
