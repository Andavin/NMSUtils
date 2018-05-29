package com.andavin.nbt.wrapper;

import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive type {@code byte}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.BYTE, params = byte.class)
public final class NBTTagByte extends NBTNumber {
    
    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagByte"), "data");
    private final byte data;

    public NBTTagByte(final byte data) {
        super(NBTHelper.createTag(NBTTagByte.class, data));
        this.data = data;
    }

    NBTTagByte(final Object wrapped) {
        super(wrapped);
        //noinspection ConstantConditions
        this.data = Reflection.getValue(DATA, wrapped);
    }

    @Override
    public Byte getData() {
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
        return data;
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
    public static NBTTagByte deserialize(final Map<String, Object> map) {
        return new NBTTagByte(((Byte) map.get("data")).byteValue());
    }
}
