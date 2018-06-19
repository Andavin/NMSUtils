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

import com.andavin.DataHolder;

/**
 * An NBT number corresponding and wrapping the
 * {@code NBTNumber} version in NMS. This will
 * hold values that are of primitive number types.
 *
 * @author Andavin
 * @since May 12, 2018
 */
public abstract class NBTNumber extends NBTBase implements DataHolder<Number> {

    NBTNumber(final Object handle) {
        super(handle);
    }

    /**
     * Get the number data stored in this object as
     * a primitive {@code long} type. If this number
     * type is of a different type it will be correctly
     * conformed and cast.
     *
     * @return The data as a long.
     */
    public abstract long asLong();

    /**
     * Get the number data stored in this object as
     * a primitive {@code int} type. If this number
     * type is of a different type it will be correctly
     * conformed and cast.
     *
     * @return The data as a int.
     */
    public abstract int asInt();

    /**
     * Get the number data stored in this object as
     * a primitive {@code short} type. If this number
     * type is of a different type it will be correctly
     * conformed and cast.
     *
     * @return The data as a short.
     */
    public abstract short asShort();

    /**
     * Get the number data stored in this object as
     * a primitive {@code byte} type. If this number
     * type is of a different type it will be correctly
     * conformed and cast.
     *
     *
     * @return The data as a byte.
     */
    public abstract byte asByte();

    /**
     * Get the number data stored in this object as
     * a primitive {@code double} type. If this number
     * type is of a different type it will be correctly
     * conformed and cast.
     *
     * @return The data as a double.
     */
    public abstract double asDouble();

    /**
     * Get the number data stored in this object as
     * a primitive {@code float} type. If this number
     * type is of a different type it will be correctly
     * conformed and cast.
     *
     * @return The data as a float.
     */
    public abstract float asFloat();

    @Override
    public final boolean isEmpty() {
        return false;
    }
}
