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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.server.v1_8_R3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static com.andavin.reflect.Reflection.getValue;

/**
 * @since December 06, 2018
 * @author Andavin
 */
public class ServerConnectionDelegate extends ServerConnection {

    private static final Logger LOGGER = LogManager.getLogger(ServerConnection.class);

    private final ServerConnection delegate;
    private final MinecraftServer server;
    private final List<ChannelFuture> futures;
    private final List<NetworkManager> networkManagers;

    public ServerConnectionDelegate(ServerConnection delegate) {
        super(delegate.d());
        this.server = delegate.d();
        this.delegate = delegate;
        this.futures = getValue(ServerConnection.class, delegate, "g");
        this.networkManagers = getValue(ServerConnection.class, delegate, "h");
        com.andavin.util.Logger.info("Created new proxy connection");
    }

    @Override
    public void a(InetAddress address, int i) throws IOException {

        synchronized (this.futures) {

            Class clazz;
            LazyInitVar lazyinitvar;
            if (Epoll.isAvailable() && this.server.ai()) {
                clazz = EpollServerSocketChannel.class;
                lazyinitvar = b;
                LOGGER.info("Using epoll channel type");
            } else {
                clazz = NioServerSocketChannel.class;
                lazyinitvar = a;
                LOGGER.info("Using default channel type");
            }

            this.futures.add(new ServerBootstrap().channel(clazz).childHandler(new ChannelInitializer() {
                @Override
                protected void initChannel(Channel channel) throws Exception {

                    try {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                    } catch (ChannelException ignored) {
                    }

                    channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                            .addLast("legacy_query", new LegacyPingHandler(ServerConnectionDelegate.this))
                            .addLast("splitter", new PacketSplitter())
                            .addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND))
                            .addLast("prepender", new PacketPrepender())
                            .addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));

                    NetworkManager networkmanager = new NetworkManagerProxy(EnumProtocolDirection.SERVERBOUND);
                    networkManagers.add(networkmanager);
                    channel.pipeline().addLast("packet_handler", networkmanager);
                    networkmanager.a(new HandshakeListener(server, networkmanager));
                }
            }).group((EventLoopGroup) lazyinitvar.c()).localAddress(address, i).bind().syncUninterruptibly());
        }
    }

    @Override
    public void b() {
        this.delegate.b();
    }

    @Override
    public void c() {
        this.delegate.c();
    }

    @Override
    public MinecraftServer d() {
        return this.delegate.d();
    }
}
