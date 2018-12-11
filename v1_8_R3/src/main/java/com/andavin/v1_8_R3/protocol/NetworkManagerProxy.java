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

import com.andavin.util.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;

/**
 * @since December 06, 2018
 * @author Andavin
 */
public class NetworkManagerProxy extends NetworkManager {

    public NetworkManagerProxy(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
        Logger.info("Created new network manager proxy");
    }

    @Override
    public void handle(Packet packet) {
        Logger.info("Sending packet {} via handle method", packet.getClass().getName());
        super.handle(packet);
    }

    @Override
    public void a(Packet packet, GenericFutureListener<? extends Future<? super Void>> genericfuturelistener,
                  GenericFutureListener<? extends Future<? super Void>>... agenericfuturelistener) {
        Logger.info("Sending packet {} via a method", packet.getClass().getName());
        super.a(packet, genericfuturelistener, agenericfuturelistener);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        Logger.info("Receiving packet {}", packet.getClass().getName());
        super.channelRead0(ctx, packet);
    }
}
