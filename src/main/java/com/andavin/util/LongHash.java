package com.andavin.util;

/**
 * @since May 28, 2018
 * @author Andavin
 */
public final class LongHash {

    /**
     * Take two integer and hash them into a single long.
     * This is not simply just shifting the bits to fit
     * in a long, but also subtracting to add a hash.
     *
     * @param msw The X value or first integer to hash.
     * @param lsw The Z value or second integer to hash.
     * @return The {@code long} hash that is created from the two integers.
     */
    public static long toLong(final int msw, final int lsw) {
        return ((long) msw << 32) + (long) lsw - Integer.MIN_VALUE;
    }

    /**
     * Retrieve the first integer (or X) that was hashed into the long.
     * The long given should be a hash retrieved from {@link #toLong(int, int)}.
     *
     * @param l The {@code long} hash to get the first integer from.
     * @return The X value or first integer that was hashed into the long.
     */
    public static int msw(final long l) {
        return (int) (l >> 32);
    }

    /**
     * Retrieve the second integer (or Z) that was hashed into the long.
     * The long given should be a hash retrieved from {@link #toLong(int, int)}.
     *
     * @param l The {@code long} hash to get the second integer from.
     * @return The Z value or first integer that was hashed into the long.
     */
    public static int lsw(final long l) {
        return (int) l + Integer.MIN_VALUE;
    }
}
