package com.andavin.nbt.wrapper;

import com.andavin.DataHolder;
import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive array type {@code byte}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.BYTE_ARRAY, params = byte[].class)
public final class NBTTagByteArray extends NBTBase implements DataHolder<byte[]> {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagByteArray"), "data");
    private final byte[] data;

    public NBTTagByteArray(final byte... data) {
        this(NBTHelper.createTag(NBTTagByteArray.class, (Object) data));
    }

    public NBTTagByteArray(final List<Byte> data) {
        this(convert(data));
    }

    NBTTagByteArray(final Object wrapped) {
        super(wrapped);
        this.data = Reflection.getValue(DATA, wrapped);
    }

    /**
     * Get the {@code byte} array that is handled by this
     * wrapper. This is the actual array object held within
     * the NMS {@code NBTTagByteArray} class. Therefore, any
     * edits made to the returned array are also made to the
     * actual data array inside the NMS object.
     *
     * @return The primitive data array.
     */
    @Override
    public byte[] getData() {
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
    public static NBTTagByteArray deserialize(final Map<String, Object> map) {
        return new NBTTagByteArray((byte[]) map.get("data"));
    }

    private static byte[] convert(final List<Byte> data) {

        final byte[] array = new byte[data.size()];
        for (int i = 0; i < array.length; i++) {
            final Byte num = data.get(i);
            array[i] = num == null ? 0 : num;
        }

        return array;
    }
}