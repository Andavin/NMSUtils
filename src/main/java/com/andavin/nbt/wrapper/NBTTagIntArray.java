package com.andavin.nbt.wrapper;

import com.andavin.DataHolder;
import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * An NBT number wrapper for the primitive array type {@code int}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.INT_ARRAY, params = int[].class)
public final class NBTTagIntArray extends NBTBase implements DataHolder<int[]> {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagIntArray"), "data");
    private final int[] data;

    public NBTTagIntArray(final int... data) {
        this(NBTHelper.createTag(NBTTagIntArray.class, (Object) data));
    }

    public NBTTagIntArray(final List<Integer> data) {
        this(convert(data));
    }

    NBTTagIntArray(final Object wrapped) {
        super(wrapped);
        this.data = Reflection.getValue(DATA, wrapped);
    }

    /**
     * Get the {@code int} array that is handled by this
     * wrapper. This is the actual array object held within
     * the NMS {@code NBTTagIntArray} class. Therefore, any
     * edits made to the returned array are also made to the
     * actual data array inside the NMS object.
     *
     * @return The primitive data array.
     */
    @Override
    public int[] getData() {
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
    public static NBTTagIntArray deserialize(final Map<String, Object> map) {
        return new NBTTagIntArray((int[]) map.get("data"));
    }

    private static int[] convert(final List<Integer> data) {

        final int[] array = new int[data.size()];
        for (int i = 0; i < array.length; i++) {
            final Integer num = data.get(i);
            array[i] = num == null ? 0 : num;
        }

        return array;
    }
}