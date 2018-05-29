package com.andavin.nbt.wrapper;

/**
 * A class containing the constants corresponding
 * to the IDs of each NBT tag type.
 *
 * @author Andavin
 * @since May 14, 2018
 */
public final class NBTType {

    /**
     * The type ID for the corresponding NBT tag that extends {@link NBTBase}.
     * This ID can be used as a reference point for any NBT tag (wrapped or NMS)
     * like in methods such as {@link NBTTagCompound#hasKey(String, int) compound
     * type check} or {@link NBTHelper#wrap(byte, Object) helper wrapping} methods.
     * <p>
     * This may also be useful in referencing NBT tag types directly in NMS
     * code as these are the actual NMS IDs for the NBT tags.
     * <p>
     * Note that {@link #LONG_ARRAY long array} is supported only on server
     * versions {@code 1.12+}.
     */
    public static final byte END = 0, BYTE = 1, SHORT = 2, INT = 3, LONG = 4, FLOAT = 5, DOUBLE = 6,
            BYTE_ARRAY = 7, STRING = 8, LIST = 9, COMPOUND = 10, INT_ARRAY = 11, LONG_ARRAY = 12, UUID = 99;
}
