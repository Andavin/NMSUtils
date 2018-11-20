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

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

/**
 * @since November 18, 2018
 * @author Andavin
 */
public class ChannelDelegate implements Channel {

    private final Channel delegate;
    private final ChannelPipeline pipelineDelegate;
    private transient EventLoop eventLoop;

    ChannelDelegate(Channel delegate) {
        this.delegate = delegate;
        this.pipelineDelegate = new PipelineDelegate(delegate.pipeline(), this);
    }

    @Override
    public EventLoop eventLoop() {

        if (this.eventLoop == null) {
            this.eventLoop = new EventLoopDelegate(this.delegate);
        }

        return this.eventLoop;
    }

    @Override
    public Channel parent() {
        return this.delegate.parent();
    }

    @Override
    public ChannelConfig config() {
        return this.delegate.config();
    }

    @Override
    public boolean isOpen() {
        return this.delegate.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return this.delegate.isRegistered();
    }

    @Override
    public boolean isActive() {
        return this.delegate.isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return this.delegate.metadata();
    }

    @Override
    public SocketAddress localAddress() {
        return this.delegate.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return this.delegate.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture() {
        return this.delegate.closeFuture();
    }

    @Override
    public boolean isWritable() {
        return this.delegate.isWritable();
    }

    @Override
    public Unsafe unsafe() {
        return this.delegate.unsafe();
    }

    @Override
    public ChannelPipeline pipeline() {
        // Give our delegate pipeline instead
        return this.pipelineDelegate;
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.delegate.alloc();
    }

    @Override
    public ChannelFuture bind(SocketAddress address) {
        return this.delegate.bind(address);
    }

    @Override
    public ChannelFuture connect(SocketAddress address) {
        return this.delegate.connect(address);
    }

    @Override
    public ChannelFuture connect(SocketAddress address, SocketAddress address1) {
        return this.delegate.connect(address, address1);
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
    public ChannelFuture bind(SocketAddress address, ChannelPromise promise) {
        return this.delegate.bind(address, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress address, ChannelPromise promise) {
        return this.delegate.connect(address, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress address, SocketAddress address1, ChannelPromise promise) {
        return this.delegate.connect(address, address1, promise);
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
    public Channel read() {
        return this.delegate.read();
    }

    @Override
    public ChannelFuture write(Object o) {
        return this.delegate.write(o);
    }

    @Override
    public ChannelFuture write(Object o, ChannelPromise promise) {
        return this.delegate.write(o, promise);
    }

    @Override
    public Channel flush() {
        return this.delegate.flush();
    }

    @Override
    public ChannelFuture writeAndFlush(Object o, ChannelPromise promise) {
        return this.delegate.writeAndFlush(o, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object o) {
        return this.delegate.writeAndFlush(o);
    }

    @Override
    public ChannelPromise newPromise() {
        return this.delegate.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return this.delegate.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return this.delegate.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable throwable) {
        return this.delegate.newFailedFuture(throwable);
    }

    @Override
    public ChannelPromise voidPromise() {
        return this.delegate.voidPromise();
    }

    @Override
    public <T> Attribute<T> attr(AttributeKey<T> key) {
        return this.delegate.attr(key);
    }

    @Override
    public int compareTo(Channel o) {
        return this.delegate.compareTo(o);
    }
}
