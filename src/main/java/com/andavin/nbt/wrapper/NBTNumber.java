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
