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
import org.bukkit.event.EventPriority;

import java.util.*;

/**
 * @since December 06, 2018
 * @author Andavin
 */
public abstract class PacketManager {

    private final Map<Class<?>, Map<EventPriority, List<PacketListener<?>>>> listeners = new HashMap<>();

    /**
     * Add the given {@link PacketListener} to the registered
     * map under the given priority.
     *
     * @param packetClass The class of the {@code Packet} that
     *                    is being listened to.
     * @param priority The {@link EventPriority} the packet should
     *                 be registered under.
     * @param listener The PacketListener to add.
     */
    final void addListener(Class<?> packetClass, EventPriority priority, PacketListener<?> listener) {
        this.listeners.computeIfAbsent(packetClass, __ -> new EnumMap<>(EventPriority.class))
                .computeIfAbsent(priority, __ -> new LinkedList<>());
    }

    /**
     * Call all of the {@link PacketListener}s for the given
     * packet that is sent to or received from the given player.
     * <p>
     * Note that, if during the processing a PacketListener does
     * not wish the packet to continue, then any PacketListeners
     * beyond that one will not be called and this method will
     * return immediately.
     *
     * @param player The player the packet was sent to
     *               or received from.
     * @param packet The packet that is being sent or received.
     * @return The packet after if has been processed by the
     *         PacketListeners. If this returns {@code null},
     *         then the packet should not continue to be processed
     *         by the server or sent to the client.
     */
    final Object call(Player player, Object packet) {

        if (packet != null) {

            Map<EventPriority, List<PacketListener<?>>> priorities = this.listeners.get(packet.getClass());
            if (priorities == null) {
                return packet;
            }

            for (EventPriority priority : EventPriority.values()) {

                List<PacketListener<?>> listeners = priorities.get(priority);
                if (listeners != null) {

                    for (PacketListener<?> listener : listeners) {

                        packet = listener.handleObj(player, packet);
                        if (packet == null) {
                            return null;
                        }
                    }
                }
            }
        }

        return packet;
    }
}
