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

package com.andavin.chat;

import com.andavin.util.MinecraftVersion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

import static com.andavin.protocol.PacketSender.sendPacket;
import static com.andavin.reflect.Reflection.*;
import static com.andavin.util.MinecraftVersion.v1_12;

/**
 * @author Andavin
 * @since April 19, 2018
 */
public final class ActionBarUtil {

    private static final Constructor<?> PACKET, CHAT;

    static {

        if (MinecraftVersion.lessThan(v1_12)) {
            CHAT = findConstructor(findMcClass("ChatComponentText"), String.class);
            PACKET = findConstructor(findMcClass("PacketPlayOutChat"),
                    findMcClass("IChatBaseComponent"), byte.class);
        } else {
            PACKET = CHAT = null;
        }
    }

    /**
     * Send an action bar message to a player. This is the message
     * that appears above their hot bar.
     *
     * @param player The player to send the message to.
     * @param msg The message to send.
     */
    public static void sendActionBar(Player player, String msg) {

        if (CHAT == null) { // 1.12+
            // Honestly this may not even be worth it. The speed difference between
            // using direct calls vs the reflection below is like less than 100ns
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
            return;
        }

        sendPacket(player, newInstance(PACKET, newInstance(CHAT, msg), (byte) 2));
    }
}
