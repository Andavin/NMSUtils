package com.andavin.nbt.wrapper;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

/**
 * The NBT tag equivalent of {@code null}.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.END)
public final class NBTTagEnd extends NBTBase {

    public NBTTagEnd() {
        super(NBTHelper.createTag(NBTTagEnd.class));
    }

    NBTTagEnd(final Object wrapped) {
        super(wrapped);
    }

    /**
     * Deserialize the map into a new NBTBase object as
     * specified by {@link ConfigurationSerializable}.
     *
     * @param map The map that was serialized with
     *        {@link ConfigurationSerializable#serialize()}.
     * @return The newly created, deserialized object.
     */
    public static NBTTagEnd deserialize(final Map<String, Object> map) {
        return new NBTTagEnd();
    }
}
