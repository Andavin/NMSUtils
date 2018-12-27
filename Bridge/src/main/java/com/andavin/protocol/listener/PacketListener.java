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

package com.andavin.protocol.listener;

import org.bukkit.entity.Player;

import java.util.function.BiFunction;

/**
 * A simple bi-directional packet listener that can handle
 * packets being sent to or coming from a player.
 * <p>
 * The direction the packet will be traveling when it is handled
 * by this listener is dependent on which of the directional
 * registration methods are used to register the listener.
 *
 * @param <T> The type of packet that is being listened for.
 * @see PacketManager#register(Class, PacketListener, ListenerPriority)
 * @since October 31, 2018
 * @author Andavin
 */
@FunctionalInterface
public interface PacketListener<T> extends BiFunction<Player, T, T> {

    /**
     * Handle a packet being sent in the direction that this
     * packet listener was registered to listen for.
     *
     * @param player The player the packet is being sent to or from.
     * @param packet The packet that is being sent.
     * @return The packet that should be sent. If this is {@code null}
     *         the packet will stop here.
     */
    @Override
    T apply(Player player, T packet);

    /**
     * Handle a packet being sent in the direction that this
     * packet listener was registered to listen for.
     *
     * @param player The player the packet is being sent to or from.
     * @param msg The packet that is being sent.
     * @return The packet that should be sent. If this is {@code null}
     *         the packet will stop here.
     */
    default T handle(Player player, Object msg) {
        return this.apply(player, (T) msg);
    }
}
