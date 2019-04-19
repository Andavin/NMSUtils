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

import com.andavin.Versioned;
import com.andavin.reflect.Reflection;
import org.bukkit.entity.Player;

/**
 * A class used exclusively for causing packets to
 * be received by the server from a specific
 * {@link Player Minecraft client}.
 *
 * @since April 19, 2019
 * @author Andavin
 */
public abstract class PacketReceiver implements Versioned {

    private static final PacketReceiver BRIDGE = Versioned.getInstance(PacketReceiver.class);

    /**
     * Receive the given packet from the given {@link Player}'s client
     * as if it was sent from the player.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#newInstance(Class, Object...)}
     * and {@link Reflection#findMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to receive the packet from.
     * @param packet The packet to receive.
     * @see Reflection
     */
    public static void receivePacket(Player player, Object packet) {
        BRIDGE.receive(player, packet);
    }

    /**
     * Receive the given packet from the given {@link Player}'s client
     * as if it was sent from the player.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#newInstance(Class, Object...)}
     * and {@link Reflection#findMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to receive the packet from.
     * @param packet The packet to receive.
     * @see Reflection
     */
    protected abstract void receive(Player player, Object packet);
}
