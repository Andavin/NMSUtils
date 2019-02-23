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

package com.andavin.v1_13_R2.protocol;

import com.andavin.inject.InjectorVersion;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.server.v1_13_R2.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @since December 06, 2018
 * @author Andavin
 */
@InjectorVersion("1.0")
public class ServerConnectionProxy extends ServerConnection {

    private static final Logger LOGGER = LogManager.getLogger(ServerConnection.class);

    private final MinecraftServer server;
    private final List<ChannelFuture> futures;
    private final List<NetworkManager> networkManagers;
    private BiFunction<Player, Packet, Packet> packetListener;

    public ServerConnectionProxy(MinecraftServer server) {

        super(server);
        this.server = server;

        try {
            Field f = ServerConnection.class.getDeclaredField("f");
            f.setAccessible(true);
            this.futures = (List<ChannelFuture>) f.get(this);
            Field g = ServerConnection.class.getDeclaredField("g");
            g.setAccessible(true);
            this.networkManagers = (List<NetworkManager>) g.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void a(InetAddress address, int i) throws IOException {

        synchronized (this.futures) {

            Class clazz;
            LazyInitVar lazyinitvar;
            if (Epoll.isAvailable() && this.server.V()) {
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
                            .addLast("legacy_query", new LegacyPingHandler(ServerConnectionProxy.this))
                            .addLast("splitter", new PacketSplitter())
                            .addLast("decoder", new PacketDecoder(EnumProtocolDirection.SERVERBOUND))
                            .addLast("prepender", new PacketPrepender())
                            .addLast("encoder", new PacketEncoder(EnumProtocolDirection.CLIENTBOUND));

                    NetworkManager networkmanager = new NetworkManagerProxy(EnumProtocolDirection.SERVERBOUND, packetListener);
                    networkManagers.add(networkmanager);
                    channel.pipeline().addLast("packet_handler", networkmanager);
                    networkmanager.setPacketListener(new HandshakeListener(server, networkmanager));
                }
            }).group((EventLoopGroup) lazyinitvar.a()).localAddress(address, i).bind().syncUninterruptibly());
        }
    }

    /**
     * Set the {@link BiFunction packet listener} for this
     * server connection that will be used for every network
     * manager and packets will be sent to.
     *
     * @param packetListener The listener to set to.
     */
    public void setPacketListener(BiFunction<Player, Packet, Packet> packetListener) {

        this.packetListener = packetListener;
        synchronized (this.networkManagers) {

            for (NetworkManager manager : this.networkManagers) {
                ((NetworkManagerProxy) manager).packetListener = packetListener;
            }
        }
    }
}
