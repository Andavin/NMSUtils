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

import io.netty.channel.*;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.andavin.protocol.Protocol.DELEGATE_DECODER;
import static com.andavin.protocol.Protocol.MINECRAFT_DECODER;

/**
 * @since November 18, 2018
 * @author Andavin
 */
public class PipelineDelegate implements ChannelPipeline {

    private final Channel channelDelegate;
    private final ChannelPipeline delegate;

    PipelineDelegate(ChannelPipeline delegate, Channel channelDelegate) {
        this.delegate = delegate;
        this.channelDelegate = channelDelegate;
    }

    @Override
    public ChannelPipeline addFirst(String name, ChannelHandler handler) {
        this.delegate.addFirst(name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addFirst(EventExecutorGroup group, String name, ChannelHandler handler) {
        this.delegate.addFirst(group, name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addLast(String name, ChannelHandler handler) {
        this.delegate.addLast(name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addLast(EventExecutorGroup group, String name, ChannelHandler handler) {
        this.delegate.addLast(group, name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler) {

        // Correct the position of the decoder (from ProtocolLib)
        if (handler != null && MINECRAFT_DECODER.equals(baseName) && this.get(DELEGATE_DECODER) != null) {

            String className = handler.getClass().getCanonicalName();
            if (className.contains("Compressor") || className.contains("Decompressor")) {
                this.delegate.addBefore(DELEGATE_DECODER, name, handler);
                return this;
            }
        }

        this.delegate.addBefore(baseName, name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addBefore(EventExecutorGroup group, String baseName, String name, ChannelHandler handler) {
        this.delegate.addBefore(group, baseName, name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler) {
        this.delegate.addAfter(baseName, name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addAfter(EventExecutorGroup group, String baseName, String name, ChannelHandler handler) {
        this.delegate.addAfter(group, baseName, name, handler);
        return this;
    }

    @Override
    public ChannelPipeline addFirst(ChannelHandler... handlers) {
        this.delegate.addFirst(handlers);
        return this;
    }

    @Override
    public ChannelPipeline addFirst(EventExecutorGroup group, ChannelHandler... handlers) {
        this.delegate.addFirst(group, handlers);
        return this;
    }

    @Override
    public ChannelPipeline addLast(ChannelHandler... handlers) {
        this.delegate.addLast(handlers);
        return this;
    }

    @Override
    public ChannelPipeline addLast(EventExecutorGroup group, ChannelHandler... handlers) {
        this.delegate.addLast(group, handlers);
        return this;
    }

    @Override
    public ChannelPipeline remove(ChannelHandler handler) {
        this.delegate.remove(handler);
        return this;
    }

    @Override
    public ChannelHandler remove(String name) {
        return this.delegate.remove(name);
    }

    @Override
    public <T extends ChannelHandler> T remove(Class<T> handlerType) {
        return this.delegate.remove(handlerType);
    }

    @Override
    public ChannelHandler removeFirst() {
        return this.delegate.removeFirst();
    }

    @Override
    public ChannelHandler removeLast() {
        return this.delegate.removeLast();
    }

    @Override
    public ChannelPipeline replace(ChannelHandler oldHandler, String newName, ChannelHandler newHandler) {
        this.delegate.replace(oldHandler, newName, newHandler);
        return this;
    }

    @Override
    public ChannelHandler replace(String oldName, String newName, ChannelHandler newHandler) {
        return this.delegate.replace(oldName, newName, newHandler);
    }

    @Override
    public <T extends ChannelHandler> T replace(Class<T> oldHandlerType, String newName, ChannelHandler newHandler) {
        return this.delegate.replace(oldHandlerType, newName, newHandler);
    }

    @Override
    public ChannelHandler first() {
        return this.delegate.first();
    }

    @Override
    public ChannelHandlerContext firstContext() {
        return this.delegate.firstContext();
    }

    @Override
    public ChannelHandler last() {
        return this.delegate.last();
    }

    @Override
    public ChannelHandlerContext lastContext() {
        return this.delegate.lastContext();
    }

    @Override
    public ChannelHandler get(String name) {
        return this.delegate.get(name);
    }

    @Override
    public <T extends ChannelHandler> T get(Class<T> handlerType) {
        return this.delegate.get(handlerType);
    }

    @Override
    public ChannelHandlerContext context(ChannelHandler handler) {
        return this.delegate.context(handler);
    }

    @Override
    public ChannelHandlerContext context(String name) {
        return this.delegate.context(name);
    }

    @Override
    public ChannelHandlerContext context(Class<? extends ChannelHandler> handlerType) {
        return this.delegate.context(handlerType);
    }

    @Override
    public Channel channel() {
        return this.channelDelegate;
    }

    @Override
    public List<String> names() {
        return this.delegate.names();
    }

    @Override
    public Map<String, ChannelHandler> toMap() {
        return this.delegate.toMap();
    }

    @Override
    public ChannelPipeline fireChannelRegistered() {
        this.delegate.fireChannelRegistered();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelUnregistered() {
        this.delegate.fireChannelUnregistered();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelActive() {
        this.delegate.fireChannelActive();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelInactive() {
        this.delegate.fireChannelInactive();
        return this;
    }

    @Override
    public ChannelPipeline fireExceptionCaught(Throwable cause) {
        this.delegate.fireExceptionCaught(cause);
        return this;
    }

    @Override
    public ChannelPipeline fireUserEventTriggered(Object event) {
        this.delegate.fireUserEventTriggered(event);
        return this;
    }

    @Override
    public ChannelPipeline fireChannelRead(Object msg) {
        this.delegate.fireChannelRead(msg);
        return this;
    }

    @Override
    public ChannelPipeline fireChannelReadComplete() {
        this.delegate.fireChannelReadComplete();
        return this;
    }

    @Override
    public ChannelPipeline fireChannelWritabilityChanged() {
        this.delegate.fireChannelWritabilityChanged();
        return this;
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return this.delegate.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return this.delegate.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return this.delegate.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return this.delegate.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return this.delegate.close();
    }

    @Override
    public ChannelFuture deregister() {
        return this.delegate.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return this.delegate.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return this.delegate.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return this.delegate.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return this.delegate.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return this.delegate.close(promise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return this.delegate.deregister(promise);
    }

    @Override
    public ChannelPipeline read() {
        this.delegate.read();
        return this;
    }

    @Override
    public ChannelFuture write(Object msg) {
        return this.delegate.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return this.delegate.write(msg, promise);
    }

    @Override
    public ChannelPipeline flush() {
        this.delegate.flush();
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return this.delegate.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return this.delegate.writeAndFlush(msg);
    }

    @Override
    public Iterator<Entry<String, ChannelHandler>> iterator() {
        return this.delegate.iterator();
    }
}
