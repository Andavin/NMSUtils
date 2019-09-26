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

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonMap;

/**
 * A basic class that holds a wrapped NBT object and that
 * every NBT wrapper should extend to give it basic NBT
 * functionality and abstraction.
 * <p>
 * All NBT tags are both {@link ConfigurationSerializable}
 * as well as {@link Serializable}. This means that when
 * working with Yaml configurations or with raw I/O streams,
 * all NBT tag objects can be written and read directly.
 *
 * @author Andavin
 * @since May 12, 2018
 */
public abstract class NBTBase<T> implements Serializable, ConfigurationSerializable {

    /**
     * This is the actual reference to the NMS NBT object.
     * Changes made to this object will be reflected in the
     * actual NBT object.
     */
    transient Object wrapped;
    private final byte typeId;

    /**
     * A constructor used to wrap the NMS NBT object. This basic
     * constructor should always exist in final child classes
     * in order to create objects by passing in a single NMS object.
     *
     * @param wrapped The NMS NBT object to wrap.
     */
    NBTBase(Object wrapped) {
        this.wrapped = wrapped;
        NBTTag type = this.getClass().getDeclaredAnnotation(NBTTag.class);
        checkState(type != null, "{} missing tag annotation", this.getClass());
        this.typeId = type.typeId();
    }

    /**
     * The ID of the type of NBT that this NBT object is. This is
     * derived from and is the same ID as the wrapped NMS NBT type.
     *
     * @return The ID of this NBT type.
     * @see NBTType NBT Type IDs
     */
    public final byte getTypeId() {
        return typeId;
    }

    /**
     * Tell if this NBT has any values stored within its container.
     * <p>
     * If this object is any kind of number, then it will always
     * return false by default. If it is a list or compound (map),
     * then it will be based on if there is any objects held in
     * those specific storage containers.
     *
     * @return Tell if this object holds any values.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Get the wrapped NMS object (similar to {@code getHandle}
     * in CraftBukkit classes).
     *
     * @return The wrapped NMS NBT object.
     */
    public final Object getWrapped() {
        return wrapped;
    }

    /**
     * Get the data that this object is holding.
     *
     * @return The data value.
     */
    public abstract T getData();

    @Override
    public final boolean equals(Object o) {
        return o instanceof NBTBase && this.wrapped.equals(((NBTBase) o).wrapped);
    }

    @Override
    public final int hashCode() {
        return this.wrapped.hashCode();
    }

    @Override
    public String toString() {
        return this.wrapped.toString();
    }

    @Override
    public Map<String, Object> serialize() {
        return singletonMap("data", this.getData());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        NBTTag type = this.getClass().getAnnotation(NBTTag.class);
        switch (type.params().length) {
            case 0:
                this.wrapped = NBTHelper.createTag(this.getClass());
                break;
            case 1:
                this.wrapped = NBTHelper.createTag(this.getClass(), this.getData());
                break;
        }
    }
}
