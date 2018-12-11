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

/**
 * @since November 19, 2018
 * @author Andavin
 */
public class ServerInjector {

    public synchronized void inject() {

//        List<Object> managers = Protocol.BRIDGE.getNetworkManagers();
        // Handle connected channels
//        ChannelInboundHandler endInitProtocol = new ChannelInitializer<Channel>() {
//            @Override
//            protected void initChannel(Channel channel) throws Exception {
//                try {
//                    synchronized (networkManagers) {
//                        // For some reason it needs to be delayed on 1.12, but the delay breaks 1.11 and below
//                        // TODO I see this more as a temporary hotfix than a permanent solution
//                        if (MinecraftVersion.getCurrentVersion().getMinor() >= 12) {
//                            channel.eventLoop().submit(() ->
//                                    injectionFactory.fromChannel(channel, ProtocolInjector.this, playerFactory).inject());
//                        } else {
//                            injectionFactory.fromChannel(channel, ProtocolInjector.this, playerFactory).inject();
//                        }
//                    }
//                } catch (Exception ex) {
//                    reporter.reportDetailed(ProtocolInjector.this, Report.newBuilder(REPORT_CANNOT_INJECT_INCOMING_CHANNEL).messageParam(channel).error(ex));
//                }
//            }
//        };

        // This is executed before Minecraft's channel handler
//        ChannelInboundHandler beginInitProtocol = new ChannelInitializer<Channel>() {
//            @Override
//            protected void initChannel(Channel channel) throws Exception {
//                // Our only job is to add init protocol
//                channel.pipeline().addLast(endInitProtocol);
//            }
//        };
//
//        // Add our handler to newly created channels
//        ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
//            @Override
//            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                Channel channel = (Channel) msg;
//
//                // Prepare to initialize ths channel
//                channel.pipeline().addFirst(beginInitProtocol);
//                ctx.fireChannelRead(msg);
//            }
//        };
//
//        List<Object> fields = Protocol.BRIDGE.getBootstrapFields();
    }
}
