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

import com.andavin.reflect.exception.UncheckedNoSuchFieldException;
import com.google.common.collect.Maps;
import io.netty.channel.*;
import io.netty.util.concurrent.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.andavin.protocol.Protocol.*;
import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getValue;

/**
 * @since November 18, 2018
 * @author Andavin
 */
class EventLoopDelegate implements EventLoop {

    private static final Object NULL = new Object();
    private static final Map<Class<?>, Object> PACKET_FIELDS = Maps.newConcurrentMap();
    private final Channel delegate;

    EventLoopDelegate(Channel delegate) {
        this.delegate = delegate;
    }

    @Override
    public EventLoopGroup parent() {
        return this.delegate.eventLoop().parent();
    }

    @Override
    public boolean inEventLoop() {
        return this.delegate.eventLoop().inEventLoop();
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return this.delegate.eventLoop().inEventLoop(thread);
    }

    @Override
    public <V> Promise<V> newPromise() {
        return this.delegate.eventLoop().newPromise();
    }

    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return this.delegate.eventLoop().newProgressivePromise();
    }

    @Override
    public <V> Future<V> newSucceededFuture(V result) {
        return this.delegate.eventLoop().newSucceededFuture(result);
    }

    @Override
    public <V> Future<V> newFailedFuture(Throwable cause) {
        return this.delegate.eventLoop().newFailedFuture(cause);
    }

    @Override
    public boolean isShuttingDown() {
        return this.delegate.eventLoop().isShuttingDown();
    }

    @Override
    public Future<?> shutdownGracefully() {
        return this.delegate.eventLoop().shutdownGracefully();
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        return this.delegate.eventLoop().shutdownGracefully();
    }

    @Override
    public Future<?> terminationFuture() {
        return this.delegate.eventLoop().terminationFuture();
    }

    @Deprecated
    @Override
    public void shutdown() {
        this.delegate.eventLoop().shutdown();
    }

    @Deprecated
    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.eventLoop().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.eventLoop().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.eventLoop().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.eventLoop().awaitTermination(timeout, unit);
    }

    @Override
    public EventLoop next() {
        return this.delegate.eventLoop().next();
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return this.delegate.eventLoop().iterator();
    }

    @Override
    public Future<?> submit(Runnable task) {
        return this.delegate.eventLoop().submit(this.scheduling(task));
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.delegate.eventLoop().invokeAll(tasks);
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.eventLoop().invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.eventLoop().invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.eventLoop().invokeAny(tasks, timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return this.delegate.eventLoop().submit(this.scheduling(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return this.delegate.eventLoop().submit(this.scheduling(task));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return this.delegate.eventLoop().schedule(this.scheduling(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return this.delegate.eventLoop().schedule(this.scheduling(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return this.delegate.eventLoop().scheduleAtFixedRate(this.scheduling(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return this.delegate.eventLoop().scheduleWithFixedDelay(this.scheduling(command), initialDelay, delay, unit);
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return this.delegate.eventLoop().register(channel);
    }

    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        return this.delegate.eventLoop().register(channel, promise);
    }

    @Override
    public void execute(Runnable command) {
        this.delegate.eventLoop().execute(this.scheduling(command));
    }

    private Runnable scheduling(Runnable runnable) {

        Field field = this.getPacketField(runnable);
        if (field != null) {
            Object packet = getValue(field, runnable);
            // TODO notify listeners
            // TODO set value if object changed
            return packet != null ? runnable : EMPTY_RUNNABLE;
        }

        return runnable;
    }

    private <T> Callable<T> scheduling(Callable<T> callable) {

        Field field = this.getPacketField(callable);
        if (field != null) {
            Object packet = getValue(field, callable);
            // TODO notify listeners
            // TODO set value if object changed
            return packet != null ? callable : (Callable<T>) EMPTY_CALLABLE;
        }

        return callable;
    }

    private Field getPacketField(Object value) {

        Object field = PACKET_FIELDS.computeIfAbsent(value.getClass(), clazz -> {

            try {
                return findField(clazz, PACKET_FIELD_MATCHER);
            } catch (UncheckedNoSuchFieldException e) {
                return NULL;
            }
        });

        return field != NULL ? (Field) field : null;
    }
}
