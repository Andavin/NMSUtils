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
import com.andavin.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @since December 06, 2018
 * @author Andavin
 */
public class ProtocolManager implements Versioned {

    private final Map<Class<?>, Map<ProtocolPriority, List<PacketListener<?>>>> nettyListeners = new HashMap<>();
    private final Map<Class<?>, Map<ProtocolPriority, List<PacketListener<?>>>> mainThreadListeners = new HashMap<>();

    protected ProtocolManager() { // This class is useless if it's not extended
    }

    /**
     * Register the given {@link PacketListener} to listen to
     * the Minecraft packet that is the given packet class under
     * the {@link ProtocolPriority#NORMAL} priority.
     *
     * @param packetClass The class of the {@code Packet} that
     *                    is being listened to.
     * @param listener The PacketListener to register.
     * @param <T> The type of packet that is being listened for.
     */
    public <T> void register(Class<T> packetClass, PacketListener<T> listener) {
        this.register(packetClass, ProtocolPriority.NORMAL, listener);
    }

    /**
     * Register the given {@link PacketListener} to listen to
     * the Minecraft packet that is the given packet class.
     *
     * @param packetClass The class of the {@code Packet} that
     *                    is being listened to.
     * @param priority The {@link ProtocolPriority} the packet should
     *                 be registered under.
     * @param listener The PacketListener to register.
     * @param <T> The type of packet that is being listened for.
     */
    public <T> void register(Class<T> packetClass, ProtocolPriority priority, PacketListener<T> listener) {
        this.nettyListeners.computeIfAbsent(packetClass, __ -> new EnumMap<>(ProtocolPriority.class))
                .computeIfAbsent(priority, __ -> new ArrayList<>(1)).add(listener);
    }

    /**
     * Register the given {@link PacketListener} to listen to
     * the Minecraft packet that is the given packet class.
     *
     * @param packetClass The class of the {@code Packet} that
     *                    is being listened to.
     * @param mainThread If the packet listener should be called
     *                   when a packet is handled on the main thread
     *                   rather than when it comes down the netty pipeline.
     * @param priority The {@link ProtocolPriority} the packet should
     *                 be registered under.
     * @param listener The PacketListener to register.
     * @param <T> The type of packet that is being listened for.
     */
    public <T> void register(Class<T> packetClass, boolean mainThread, ProtocolPriority priority, PacketListener<T> listener) {
        Map<Class<?>, Map<ProtocolPriority, List<PacketListener<?>>>> listeners =
                mainThread ? this.mainThreadListeners : this.nettyListeners;
        listeners.computeIfAbsent(packetClass, __ -> new EnumMap<>(ProtocolPriority.class))
                .computeIfAbsent(priority, __ -> new ArrayList<>(1)).add(listener);
    }

    /**
     * Unregister the given {@link PacketListener} so that it
     * no longer is called when a packet is received or sent.
     *
     * @param listener The PacketListener to unregister.
     */
    public void unregister(PacketListener<?> listener) {
        // Remove the listener
        // Remove the list of listeners if it contained the listener (remove it) and is now empty
        // Remove the priority map if it removed a listener list and is now empty
        this.nettyListeners.values().removeIf(priorities -> priorities.values().removeIf(listeners ->
                listeners.remove(listener) && listeners.isEmpty()) && priorities.isEmpty());
        this.mainThreadListeners.values().removeIf(priorities -> priorities.values().removeIf(listeners ->
                listeners.remove(listener) && listeners.isEmpty()) && priorities.isEmpty());
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
    protected final Object call(Player player, Object packet) {

        if (packet != null) {

            Class<?> clazz = packet.getClass();
            Map<Class<?>, Map<ProtocolPriority, List<PacketListener<?>>>> listenerMap =
                    Bukkit.isPrimaryThread() ? this.mainThreadListeners : this.nettyListeners;
            Map<ProtocolPriority, List<PacketListener<?>>> priorities = listenerMap.get(clazz);
            if (priorities == null) {
                return packet;
            }

            for (List<PacketListener<?>> listeners : priorities.values()) { // Iterates in natural order

                for (PacketListener<?> listener : listeners) {

                    try {
                        packet = listener.handleMsg(player, packet);
                    } catch (Throwable e) {
                        Logger.severe(e, "Exception thrown by packet listener {} for packet {}", listener.getClass(), clazz);
                    }

                    if (packet == null) {
                        return null;
                    }
                    // If the packet differs in type from the original packet
                    // then call the listeners on the new type of packet
                    if (clazz != packet.getClass()) {
                        return this.call(player, packet);
                    }
                }
            }
        }

        return packet;
    }
}
