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

package com.andavin.protocol;

/**
 * A priority to differentiate between different levels of
 * {@link PacketListener}s while they are called.
 * <p>
 * This works similarly to the Bukkit {@code EventPriority}.
 *
 * @since December 12, 2018
 * @author Andavin
 */
public enum ListenerPriority {

    /**
     * Packet listener call is of very low importance and should be ran first,
     * to allow other listeners to further customise the outcome.
     */
    LOWEST,

    /**
     * Packet listener call is of low importance.
     */
    LOW,

    /**
     * Packet listener call is neither important or unimportant, and may be
     * ran normally.
     */
    NORMAL,

    /**
     * Packet listener call is of high importance.
     */
    HIGH,

    /**
     * Packet listener call is critical and must have the final say in what happens
     * to the packet.
     */
    HIGHEST,

    /**
     * Packet is listened to purely for monitoring the outcome of an packet.
     * <p>
     * No modifications to the packet should be made under this priority.
     */
    MONITOR
}
