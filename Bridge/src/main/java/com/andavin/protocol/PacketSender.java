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

import java.util.List;

/**
 * A class used exclusively for sending packets to
 * {@link Player Minecraft clients}.
 * <p>
 * This class uses the most common procedure for sending
 * packets to clients through the {@code PlayerConnection}.
 * This allows for Minecraft to process the packet normally
 * and not bypass any regular checks.
 *
 * @since May 29, 2018
 * @author Andavin
 */
public abstract class PacketSender extends Versioned {

    private static final PacketSender BRIDGE = Versioned.getInstance(PacketSender.class);

    /**
     * Send the given packet to the given {@link Player}'s client.
     * The packet will be sent through the player's connection.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#newInstance(Class, Object...)}
     * and {@link Reflection#findMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to send the packet to.
     * @param packet The packet to send.
     * @see Reflection
     */
    public static void sendPacket(Player player, Object packet) {
        BRIDGE.send(player, packet);
    }

    /**
     * Send the given packets to the given {@link Player}'s client.
     * The packets will be sent through the player's connection.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#newInstance(Class, Object...)}
     * and {@link Reflection#findMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to send the packet to.
     * @param packets The list of packets to send.
     * @see Reflection
     */
    public static void sendPackets(Player player, List<Object> packets) {
        BRIDGE.send(player, packets);
    }

    /**
     * Send the given packet to the given {@link Player}'s client.
     * The packet will be sent through the player's connection.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#newInstance(Class, Object...)}
     * and {@link Reflection#findMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to send the packet to.
     * @param packet The packet to send.
     * @see Reflection
     */
    protected abstract void send(Player player, Object packet);

    /**
     * Send the given packets to the given {@link Player}'s client.
     * The packets will be sent through the player's connection.
     * <p>
     * The packet must be a Minecraft packet that extends the NMS
     * {@code Packet}. If a direct instantiation of a packet needs
     * to be avoided, the {@link Reflection#newInstance(Class, Object...)}
     * and {@link Reflection#findMcClass(String)} or similar methods
     * can be used to create an instance.
     *
     * @param player The player to send the packet to.
     * @param packets The list of packets to send.
     * @see Reflection
     */
    protected abstract void send(Player player, List<Object> packets);
}
