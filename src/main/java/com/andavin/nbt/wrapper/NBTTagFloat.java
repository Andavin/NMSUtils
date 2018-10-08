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

    public NBTTagFloat(float data) {
        super(NBTHelper.createTag(NBTTagFloat.class, data));
        this.data = data;
    }

    NBTTagFloat(Object wrapped) {
        super(wrapped);
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
    public static NBTTagFloat deserialize(Map<String, Object> map) {
        return new NBTTagFloat(((Float) map.get("data")).floatValue());
    }
}
