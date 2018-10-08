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
    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + (long) lsw - Integer.MIN_VALUE;
    }

    /**
     * Retrieve the first integer (or X) that was hashed into the long.
     * The long given should be a hash retrieved from {@link #toLong(int, int)}.
     *
     * @param l The {@code long} hash to get the first integer from.
     * @return The X value or first integer that was hashed into the long.
     */
    public static int msw(long l) {
        return (int) (l >> 32);
    }

    /**
     * Retrieve the second integer (or Z) that was hashed into the long.
     * The long given should be a hash retrieved from {@link #toLong(int, int)}.
     *
     * @param l The {@code long} hash to get the second integer from.
     * @return The Z value or first integer that was hashed into the long.
     */
    public static int lsw(long l) {
        return (int) l + Integer.MIN_VALUE;
    }
}
