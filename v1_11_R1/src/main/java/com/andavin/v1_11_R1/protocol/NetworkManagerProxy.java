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

package com.andavin.v1_11_R1.protocol;

import com.andavin.inject.InjectorVersion;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

/**
 * @since December 06, 2018
 * @author Andavin
 */
@InjectorVersion("1.0")
public class NetworkManagerProxy extends NetworkManager {

    public BiFunction<Player, Packet, Packet> packetListener;
    private String name;

    public NetworkManagerProxy(EnumProtocolDirection enumprotocoldirection, BiFunction<Player, Packet, Packet> packetListener) {
        super(enumprotocoldirection);
        this.packetListener = packetListener;
    }

    @Override
    public void sendPacket(Packet<?> packet) {

        packet = this.handleListener(packet);
        if (packet != null) {
            super.sendPacket(packet);
        }
    }

    @SafeVarargs
    @Override
    public final void sendPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener,
                                 GenericFutureListener<? extends Future<? super Void>>... agenericfuturelistener) {

        packet = this.handleListener(packet);
        if (packet != null) {
            super.sendPacket(packet, genericfuturelistener, agenericfuturelistener);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {

        // When the channel is not open, the packet
        // won't be handled by the super method
        if (this.channel.isOpen()) {

            packet = this.handleListener(packet);
            if (packet == null) {
                return;
            }
        }

        super.channelRead0(ctx, packet);
    }

    private Packet handleListener(Packet packet) {

        if (this.name == null) {

            if (packet instanceof PacketLoginInStart) {
                this.name = ((PacketLoginInStart) packet).a().getName();
            }
        } else if (this.packetListener != null) {

            PacketListener listener = this.i();
            if (listener instanceof PlayerConnection) {

                try {
                    return this.packetListener.apply(((PlayerConnection) listener).player.getBukkitEntity(), packet);
                } catch (Throwable e) {
                    MinecraftServer.LOGGER.error("Exception thrown while handling packet listener.", e);
                }
            }
        }

        return packet;
    }
}
