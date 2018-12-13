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

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;

/**
 * @since December 12, 2018
 * @author Andavin
 */
public class ProtocolLibManager extends PacketManager {

    private final Plugin plugin;
    private final ProtocolManager manager;
    private final Map<PacketListener<?>, PacketAdapter> adapters = new HashMap<>();

    public ProtocolLibManager(Plugin plugin) {
        this.plugin = plugin;
        this.manager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public <T> void register(Class<T> packetClass, PacketListener<T> listener, ListenerPriority priority) {

        PacketAdapter adapter = new PacketAdapter(this.plugin, toProtocolLib(priority),
                singletonList(PacketRegistry.getPacketType(packetClass)),
                ListenerOptions.ASYNC, ListenerOptions.SKIP_PLUGIN_VERIFIER) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                this.handle(event);
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                this.handle(event);
            }

            private void handle(PacketEvent event) {

                PacketContainer container = event.getPacket();
                Object packet = call(event.getPlayer(), container.getHandle());
                if (packet == null) {
                    event.setCancelled(true);
                } else if (packet != container.getHandle()) {
                    event.setPacket(new PacketContainer(container.getType(), packet));
                }
            }
        };

        this.adapters.put(listener, adapter);
        this.manager.addPacketListener(adapter);
        super.register(packetClass, listener, priority);
    }

    @Override
    public void unregister(PacketListener<?> listener) {

        super.unregister(listener);
        PacketAdapter adapter = this.adapters.remove(listener);
        if (adapter != null) {
            this.manager.removePacketListener(adapter);
        }
    }

    private com.comphenix.protocol.events.ListenerPriority toProtocolLib(ListenerPriority priority) {

        switch (priority) {
            case LOWEST:
                return com.comphenix.protocol.events.ListenerPriority.LOWEST;
            case LOW:
                return com.comphenix.protocol.events.ListenerPriority.LOW;
            default: // Normal is the default
            case NORMAL:
                return com.comphenix.protocol.events.ListenerPriority.NORMAL;
            case HIGH:
                return com.comphenix.protocol.events.ListenerPriority.HIGH;
            case HIGHEST:
                return com.comphenix.protocol.events.ListenerPriority.HIGHEST;
            case MONITOR:
                return com.comphenix.protocol.events.ListenerPriority.MONITOR;
        }
    }
}
