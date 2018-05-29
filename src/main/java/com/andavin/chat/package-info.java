/**
 * ChatComponent APIs. This includes wrappers for
 * Minecraft's IChatBaseComponent where you can create
 * click or hover events as well as do other formatting.
 * <p>
 * Bukkit neglected to include an API for this. Spigot
 * did add an API that can be found with the {@link net.md_5.bungee.api.chat.BaseComponent}
 * API which is very useful, however, the advantage here
 * is that {@link com.andavin.chat.ChatComponent}s can
 * be used to wrap and directly interact with NMS objects and
 * change them. This would be useful if, say, it is needed to
 * edit the object from a chat packet while it goes through without
 * actually replacing the whole object or even replacing it since
 * that functionality is not available with Spigot's API either.
 * <p>
 * This package also provides other useful utilities that are
 * commonly used to edit chat messages or other string forms
 * of communication to players that are not readily available
 * via sanctioned Bukkit or Spigot APIs.
 */
package com.andavin.chat;