package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive type {@code double}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.DOUBLE, params = double.class)
public final class NBTTagDouble extends NBTNumber {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagDouble"), "data");
    private final double data;

    public NBTTagDouble(final double data) {
        super(NBTHelper.createTag(NBTTagDouble.class, data));
        this.data = data;
    }

    NBTTagDouble(final Object wrapped) {
        super(wrapped);
        //noinspection ConstantConditions
        this.data = Reflection.getValue(DATA, wrapped);
    }

    @Override
    public Double getData() {
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
        return (float) data;
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
    public static NBTTagDouble deserialize(final Map<String, Object> map) {
        return new NBTTagDouble(((Double) map.get("data")).doubleValue());
    }
}
