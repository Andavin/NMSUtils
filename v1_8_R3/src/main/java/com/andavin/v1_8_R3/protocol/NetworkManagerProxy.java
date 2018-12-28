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

package com.andavin.v1_8_R3.protocol;

import com.andavin.inject.InjectorVersion;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketLoginInStart;

import java.util.function.BiFunction;

/**
 * @since December 06, 2018
 * @author Andavin
 */
@InjectorVersion("1.1")
public class NetworkManagerProxy extends NetworkManager {

    public BiFunction<String, Packet, Packet> packetListener;
    private GameProfile profile;

    public NetworkManagerProxy(EnumProtocolDirection enumprotocoldirection, BiFunction<String, Packet, Packet> packetListener) {
        super(enumprotocoldirection);
        this.packetListener = packetListener;
    }

    @Override
    public void handle(Packet packet) {

        packet = this.handleListener(packet);
        if (packet != null) {
            super.handle(packet);
        }
    }

    @SafeVarargs
    @Override
    public final void a(Packet packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener,
                        GenericFutureListener<? extends Future<? super Void>>... agenericfuturelistener) {

        packet = this.handleListener(packet);
        if (packet != null) {
            super.a(packet, genericfuturelistener, agenericfuturelistener);
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

        if (this.profile == null) {

            if (packet instanceof PacketLoginInStart) {
                this.profile = ((PacketLoginInStart) packet).a();
            }
        } else if (this.packetListener != null) {

            Packet altered = this.packetListener.apply(this.profile.getName(), packet);
            if (altered == null) {
                return null;
            }

            if (altered != packet) {

                Class<? extends Packet> oldClass = packet.getClass();
                Class<? extends Packet> alteredClass = altered.getClass();
                if (alteredClass != oldClass) {
                    throw new IllegalArgumentException("Packet returned expected type " +
                            oldClass.getName() + ", but got " + alteredClass.getName());
                }
            }

            return altered;
        }

        return packet;
    }
}
