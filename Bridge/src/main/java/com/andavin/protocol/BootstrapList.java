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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;

import java.util.*;

/**
 * @since December 06, 2018
 * @author Andavin
 */
public class BootstrapList implements List<ChannelFuture> {

    private final List<ChannelFuture> delegate;
    private final ChannelHandler handler;

    public BootstrapList(List<ChannelFuture> delegate, ChannelHandler handler) {
        this.delegate = delegate;
        this.handler = handler;
    }

    /**
     * Get the delegate list that the values of are being
     * bootstrap processed.
     *
     * @return The delegate list.
     */
    public List<ChannelFuture> getDelegate() {
        return delegate;
    }

    @Override
    public synchronized boolean add(ChannelFuture element) {
        this.bootstrap(element);
        return delegate.add(element);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends ChannelFuture> collection) {
        List<? extends ChannelFuture> other = new ArrayList<>(collection);
        other.forEach(this::bootstrap);
        return delegate.addAll(other);
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends ChannelFuture> c) {
        return delegate.addAll(index, c);
    }

    @Override
    public synchronized ChannelFuture set(int index, ChannelFuture element) {

        ChannelFuture old = this.delegate.set(index, element);
        // Handle the old future, and the newly inserted future
        if (old != element) {
            revert(old);
            bootstrap(element);
        }

        return old;
    }

    @Override
    public synchronized int size() {
        return delegate.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public synchronized boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
    }

    @Override
    public synchronized ChannelFuture get(int index) {
        return delegate.get(index);
    }

    @Override
    public synchronized void add(int index, ChannelFuture element) {
        delegate.add(index, element);
    }

    @Override
    public synchronized ChannelFuture remove(int index) {
        return delegate.remove(index);
    }

    @Override
    public synchronized int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public synchronized ListIterator<ChannelFuture> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public synchronized ListIterator<ChannelFuture> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public synchronized Iterator<ChannelFuture> iterator() {
        return delegate.iterator();
    }

    @Override
    public synchronized List<ChannelFuture> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }

    @Override
    public synchronized Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    /**
     * Close and revert all elements that have
     * been bootstrapped.
     */
    public synchronized void close() {
        this.forEach(this::revert);
    }

    protected void bootstrap(ChannelFuture future) {
        future.channel().pipeline().addFirst(handler);
    }

    protected void revert(ChannelFuture future) {

        Channel channel = future.channel();
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(handler);
            return null;
        });
    }
}
