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

import org.bukkit.entity.Player;

/**
 * A simple bi-directional packet listener that can handle
 * packets being sent to or coming from a player.
 * <p>
 * The direction the packet will be traveling is determined
 * by the packet itself as each type of packet has a specific
 * direction in which it is always sent denoted by the {@code In}
 * or {@code Out} in the packet class name (e.g. {@code PacketPlayInArmAnimation}
 * or {@code PacketLoginOutCustomPayload}).
 * <p>
 * The ability of this listener class is limited to packets
 * that are sent or received after the {@link Player} object is
 * created (as the Player object is included in the handler).
 * Therefore, any packets sent before it is created, for example
 * {@code PacketLoginInStart} or {@code PacketLoginOutEncryptionBegin},
 * cannot be listened to via this class. These include most packets
 * that are not included in the {@code Play} or {@code Status} protocol
 * categories (i.e. {@code Login} or {@code Handshaking}).
 * <p>
 * Note that it is immensely important that thread blocking
 * does <b>not</b> occur within one of these listeners as it
 * will block all network traffic to or from the player and can
 * severely lag their gameplay.
 *
 * @param <T> The type of packet that is being listened for.
 * @see ProtocolManager#register(Class, ProtocolPriority, PacketListener)
 * @since October 31, 2018
 * @author Andavin
 */
@FunctionalInterface
public interface PacketListener<T> {

    /**
     * Handle a packet being sent in the direction that this
     * packet listener was registered to listen for.
     *
     * @param player The player the packet is being sent to or from.
     * @param packet The packet that is being sent.
     * @return The packet that should be sent. In order to stop a packet
     *         from continuing to be sent or received {@code null} should
     *         be returned from this method.
     */
    T handle(Player player, T packet);

    /**
     * Handle a packet being sent in the direction that this
     * packet listener was registered to listen for.
     *
     * @param player The player the packet is being sent to or from.
     * @param msg The packet that is being sent.
     * @return The packet that should be sent. In order to stop a packet
     *         from continuing to be sent or received {@code null} should
     *         be returned from this method.
     */
    default T handleMsg(Player player, Object msg) {
        return this.handle(player, (T) msg);
    }
}
