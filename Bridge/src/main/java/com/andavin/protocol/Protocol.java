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

import com.andavin.MinecraftVersion;
import com.andavin.Versioned;
import com.andavin.protocol.listener.PacketListener;
import com.andavin.reflect.FieldMatcher;
import io.netty.channel.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.andavin.reflect.Reflection.findMcClass;

/**
 * @since November 18, 2018
 * @author Andavin
 */
public abstract class Protocol implements Versioned {

    public static final String MINECRAFT_DECODER = "decoder";
    public static final String MINECRAFT_ENCODER = "encoder";
    public static final String DELEGATE_DECODER = "nms_utils_decoder";
    public static final String DELEGATE_ENCODER = "nms_utils_encoder";

    public static final Runnable EMPTY_RUNNABLE = () -> {
    };
    public static final Callable<?> EMPTY_CALLABLE = () -> {
        return null;
    };

    static final FieldMatcher PACKET_FIELD_MATCHER = new FieldMatcher(findMcClass("Packet"));
    static Protocol BRIDGE;
    private static final Map<Class<?>, PacketListener<?>>

    public static void inject() {

        if (BRIDGE != null) {
            BRIDGE.closeInternal();
        }

        BRIDGE = Versioned.getInstance(Protocol.class);
        // Handle connected channels
        ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                try {
                    synchronized (networkManagers) {
                        // For some reason it needs to be delayed on 1.12, but the delay breaks 1.11 and below
                        // TODO I see this more as a temporary hotfix than a permanent solution
                        if (MinecraftVersion.getCurrentVersion().getMinor() >= 12) {
                            channel.eventLoop().submit(() ->
                                    injectionFactory.fromChannel(channel, ProtocolInjector.this, playerFactory).inject());
                        } else {
                            injectionFactory.fromChannel(channel, ProtocolInjector.this, playerFactory).inject();
                        }
                    }
                } catch (Exception ex) {
                    reporter.reportDetailed(ProtocolInjector.this, Report.newBuilder(REPORT_CANNOT_INJECT_INCOMING_CHANNEL).messageParam(channel).error(ex));
                }
            }
        };

        // This is executed before Minecraft's channel handler
        ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                // Our only job is to add init protocol
                channel.pipeline().addLast(endInitProtocol);
            }
        };

        // Add our handler to newly created channels
        ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                Channel channel = (Channel) msg;
                // Prepare to initialize ths channel
                channel.pipeline().addFirst(beginInitProtocol);
                ctx.fireChannelRead(msg);
            }
        };

        BRIDGE.injectInternal(connectionHandler);
    }

    public static void close() {
        BRIDGE.closeInternal();
        BRIDGE = null;
    }

    protected abstract void injectInternal(ChannelHandler handler);

    protected abstract List<Object> getNetworkManagers();

    protected abstract void closeInternal();
}
