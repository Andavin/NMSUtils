/*
 * MIT License
 *
 * Copyright (c) 2018 Andavin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.andavin.nbt.wrapper;

import com.andavin.reflect.FieldMatcher;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static com.andavin.reflect.Reflection.*;

/**
 * An NBT number wrapper for the primitive array type {@code long}.
 * This simply holds the value and the wrapped NMS object.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.LONG_ARRAY, params = long[].class)
public final class NBTTagLongArray extends NBTBase<long[]> {

    private static final Field DATA = findField(findMcClass("NBTTagLongArray"), new FieldMatcher(long[].class));
    private final long[] data;

    public NBTTagLongArray(long... data) {
        this(NBTHelper.createTag(NBTTagLongArray.class, data));
    }

    public NBTTagLongArray(List<Long> data) {
        this(convert(data));
    }

    NBTTagLongArray(Object wrapped) {
        super(wrapped);
        this.data = getFieldValue(DATA, wrapped);
    }

    /**
     * Get the {@code long} array that is handled by this
     * wrapper. This is the actual array object held within
     * the NMS {@code NBTTagLongArray} class. Therefore, any
     * edits made to the returned array are also made to the
     * actual data array inside the NMS object.
     *
     * @return The primitive data array.
     */
    @Override
    public long[] getData() {
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
    public static NBTTagLongArray deserialize(Map<String, Object> map) {
        return new NBTTagLongArray((long[]) map.get("data"));
    }

    private static long[] convert(List<Long> data) {

        int i = 0;
        long[] array = new long[data.size()];
        for (Long num : data) { // Optimized iterations vs get(i) for some List impl
            array[i++] = num != null ? num : 0;
        }

        return array;
    }
}
