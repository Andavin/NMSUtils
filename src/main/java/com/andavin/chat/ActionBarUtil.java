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

import com.andavin.reflect.Reflection;
import com.andavin.util.PacketSender;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

/**
 * @author Andavin
 * @since April 19, 2018
 */
public final class ActionBarUtil {

    private static final Constructor<?> PACKET, CHAT;

    static {

        if (Reflection.VERSION_NUMBER < Reflection.v1_12) {
            CHAT = Reflection.getConstructor(Reflection.getMcClass("ChatComponentText"), String.class);
            PACKET = Reflection.getConstructor(Reflection.getMcClass("PacketPlayOutChat"),
                    Reflection.getMcClass("IChatBaseComponent"), byte.class);
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
    public static void sendActionBar(final Player player, final String msg) {

        if (CHAT == null) { // 1.12+
            // Honestly this may not even be worth it. The speed difference between
            // using direct calls vs the reflection below is like less than 100ns
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
            return;
        }

        PacketSender.sendPacket(player, Reflection.getInstance(PACKET, Reflection.getInstance(CHAT, msg), (byte) 2));
    }
}
