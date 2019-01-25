///*
// * MIT License
// *
// * Copyright (c) 2018 Andavin
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
///**
// * ChatComponent APIs. This includes wrappers for
// * Minecraft's IChatBaseComponent where you can create
// * click or hover events as well as do other formatting.
// * <p>
// * Bukkit neglected to include an API for this. Spigot
// * did add an API that can be found with the {@link net.md_5.bungee.api.chat.BaseComponent}
// * API which is very useful, however, the advantage here
// * is that {@link com.andavin.chat.ChatComponent}s can
// * be used to wrap and directly interact with NMS objects and
// * change them. This would be useful if, say, it is needed to
// * edit the object from a chat packet while it goes through without
// * actually replacing the whole object or even replacing it since
// * that functionality is not available with Spigot's API either.
// * <p>
// * This package also provides other useful utilities that are
// * commonly used to edit chat messages or other string forms
// * of communication to players that are not readily available
// * via sanctioned Bukkit or Spigot APIs.
// */
//package com.andavin.chat;