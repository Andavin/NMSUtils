package com.andavin.nbt.wrapper;

import com.andavin.DataHolder;
import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An NBT wrapper for the {@link String}. This simply holds
 * the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.STRING, params = String.class)
public final class NBTTagString extends NBTBase implements DataHolder<String> {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagString"), "data");
    private final String data;

    public NBTTagString(final String data) {
        super(NBTHelper.createTag(NBTTagString.class, data));
        this.data = data;
    }

    NBTTagString(final Object wrapped) {
        super(wrapped);
        //noinspection ConstantConditions
        this.data = Reflection.getValue(DATA, wrapped);
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return data;
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
        return new NBTTagByte((String) map.get("data"));
    }
}
