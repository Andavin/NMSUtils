package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive type {@code short}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.SHORT, params = short.class)
public final class NBTTagShort extends NBTNumber {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagShort"), "data");
    private final short data;

    public NBTTagShort(final short data) {
        super(NBTHelper.createTag(NBTTagShort.class, data));
        this.data = data;
    }

    NBTTagShort(final Object wrapped) {
        super(wrapped);
        //noinspection ConstantConditions
        this.data = Reflection.getValue(DATA, wrapped);
    }

    @Override
    public Number getData() {
        return data;
    }

    @Override
    public long asLong() {
        return data;
    }

    @Override
    public int asInt() {
        return data;
    }

    @Override
    public short asShort() {
        return data;
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
    public static NBTTagShort deserialize(final Map<String, Object> map) {
        return new NBTTagShort(((Short) map.get("data")).shortValue());
    }
}
