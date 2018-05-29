package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive type {@code float}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.FLOAT, params = float.class)
public final class NBTTagFloat extends NBTNumber {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagFloat"), "data");
    private final float data;

    public NBTTagFloat(final float data) {
        super(NBTHelper.createTag(NBTTagFloat.class, data));
        this.data = data;
    }

    NBTTagFloat(final Object wrapped) {
        super(wrapped);
        //noinspection ConstantConditions
        this.data = Reflection.getValue(DATA, wrapped);
    }

    @Override
    public Float getData() {
        return data;
    }

    @Override
    public long asLong() {
        return (long) Math.floor(data);
    }

    @Override
    public int asInt() {
        return (int) Math.floor(data);
    }

    @Override
    public short asShort() {
        return (short) Math.floor(data);
    }

    @Override
    public byte asByte() {
        return (byte) Math.floor(data);
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
        return Double.toString(data);
    }

    /**
     * Deserialize the map into a new NBTBase object as
     * specified by {@link ConfigurationSerializable}.
     *
     * @param map The map that was serialized with
     *        {@link ConfigurationSerializable#serialize()}.
     * @return The newly created, deserialized object.
     */
    public static NBTTagFloat deserialize(final Map<String, Object> map) {
        return new NBTTagFloat(((Float) map.get("data")).floatValue());
    }
}
