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

import com.andavin.protocol.BootstrapList;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.ServerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;

import java.lang.reflect.Field;
import java.util.List;

import static com.andavin.reflect.Reflection.*;

/**
 * @since December 06, 2018
 * @author Andavin
 */
public class Protocol extends com.andavin.protocol.Protocol {

    private static final Field FUTURES = findField(ServerConnection.class, "g");
    private static final Field NETWORK_MANAGERS = findField(ServerConnection.class, "h");

    @Override
    protected void injectInternal(ChannelHandler handler) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerConnection serverConnection = server.aq();
        List<ChannelFuture> futures = getValue(FUTURES, serverConnection);
        setValue(FUTURES, serverConnection, new BootstrapList(futures, handler)); // Replace the future list
    }

    @Override
    protected List<Object> getNetworkManagers() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerConnection serverConnection = server.aq();
        return getValue(NETWORK_MANAGERS, serverConnection);
    }

    @Override
    protected void closeInternal() {
        getValue()
    }
}
