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

package com.andavin.util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A class to make using the {@link BukkitScheduler} less
 * cumbersome and easier to use. Also, allows for easy
 * {@code do-while} loops and similar condition based looping
 * in timed loop tasks.
 *
 * @author Andavin
 * @since May 29, 2018
 */
@SuppressWarnings("UnusedReturnValue")
public final class Scheduler {

    private static final int SEARCH = 10;
    private static final List<Task> TASKS = new LinkedList<>();
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(4);

    static {
        Scheduler.repeatAsync(() -> TASKS.removeIf(Task::cancel), 0L, 1L);
    }

    /**
     * Run a task synchronously on the main thread using
     * the {@link BukkitScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     * @return The {@link BukkitTask} that is returned after registering the task.
     */
    public static BukkitTask sync(final Runnable run) {
        return Bukkit.getScheduler().runTask(PluginRegistry.getPlugin(SEARCH), run);
    }

    /**
     * Run a task asynchronously on a separate thread using
     * the {@link BukkitScheduler}.
     *
     * @param run The {@link Runnable} to execute.
     * @return The {@link BukkitTask} that is returned after registering the task.
     */
    public static BukkitTask async(final Runnable run) {
        return Bukkit.getScheduler().runTaskAsynchronously(PluginRegistry.getPlugin(SEARCH), run);
    }

    /**
     * Run a task synchronously after a specified amount of
     * ticks using the {@link BukkitScheduler}.
     *
     * @param run The {@link Runnable} task to execute.
     * @param delay The ticks (1 tick = 50 milliseconds, 20 ticks = 1 second) after which to run the task.
     * @return The {@link BukkitTask} that is returned after registering the task.
     */
    public static BukkitTask later(final Runnable run, final long delay) {
        return Bukkit.getScheduler().runTaskLater(PluginRegistry.getPlugin(SEARCH), run, delay);
    }

    /**
     * Run a task asynchronously after a specified amount of
     * ticks using the {@link BukkitScheduler}.
     *
     * @param run The {@link Runnable} task to execute.
     * @param delay The ticks (1 tick = 50 milliseconds, 20 ticks = 1 second) after which to run the task.
     * @return The {@link BukkitTask} that is returned after registering the task.
     */
    public static BukkitTask laterAsync(final Runnable run, final long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(PluginRegistry.getPlugin(SEARCH), run, delay);
    }

    /**
     * Run a task synchronously repeatedly after a specified amount
     * of ticks and repeated every period ticks until cancelled.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @return The {@link BukkitTask} that is returned after registering the task.
     */
    public static BukkitTask repeat(final Runnable run, final long delay, final long period) {
        return Bukkit.getScheduler().runTaskTimer(PluginRegistry.getPlugin(SEARCH), run, delay, period);
    }

    /**
     * Run a task asynchronously repeatedly after a specified amount
     * of ticks and repeated every period ticks until canceled.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @return The {@link BukkitTask} that is returned after registering the task.
     */
    public static BukkitTask repeatAsync(final Runnable run, final long delay, final long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(PluginRegistry.getPlugin(SEARCH), run, delay, period);
    }

    /**
     * Run a task synchronously repeatedly until the condition is met
     * at which point it will be cancelled.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is met. The condition is always tested after each
     * run. Therefore, the task will always run at least once before ending
     * the task. For example, <pre>
     *     int k = 10;
     *     () -&gt; k == 10
     * </pre>
     * In this case, the condition is already met, however, the task will
     * still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @param until The {@link Supplier} to test when to cancel. When this is {@code true} the task will be cancelled.
     * @return The {@link BukkitTask task} that was scheduled.
     */
    public static BukkitTask repeatUntil(final Runnable run, final long delay,
            final long period, final Supplier<Boolean> until) {
        final BukkitTask task = Scheduler.repeat(run, delay, period);
        TASKS.add(new Task(true, task, until));
        return task;
    }

    /**
     * Run a task asynchronously repeatedly until the condition is met
     * at which point it will be cancelled.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is met. The condition is always tested after each
     * run. Therefore, the task will always run at least once before ending
     * the task. For example, <pre>
     *     int k = 10;
     *     () -&gt; k == 10
     * </pre>
     * In this case, the condition is already met, however, the task will
     * still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @param until The {@link Supplier} to test when to cancel.
     *         When this returns {@code true} the task will be cancelled.
     * @return The {@link BukkitTask task} that was scheduled.
     */
    public static BukkitTask repeatAsyncUntil(final Runnable run, final long delay,
            final long period, final Supplier<Boolean> until) {
        final BukkitTask task = Scheduler.repeatAsync(run, delay, period);
        TASKS.add(new Task(true, task, until));
        return task;
    }

    /**
     * Run a task repeatedly (every period of ticks) as long as the condition
     * met. Once the condition is no longer met (returns {@code false}) the
     * loop will end.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is no longer met. The condition is always tested after
     * each run. Therefore, the task will always run at least once before
     * ending the task. For example, <pre>
     *     int k = 10;
     *     () -&gt; k != 10
     * </pre>
     * In this case, the condition starts out as {@code false}, however, the
     * task will still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @param condition The {@link Supplier condition} that must be {@code true}
     *         in order for the task to continue to run.
     * @return The {@link BukkitTask task} that was scheduled.
     */
    public static BukkitTask repeatWhile(final Runnable run, final long delay,
            final long period, final Supplier<Boolean> condition) {
        final BukkitTask task = Scheduler.repeat(run, delay, period);
        TASKS.add(new Task(false, task, condition));
        return task;
    }

    /**
     * Run a task asynchronously repeatedly (every period of ticks) as long
     * as the condition met. Once the condition is no longer met (returns
     * {@code false}) the loop will end.
     * <p>
     * The task will always be executed at least once and will end once
     * the condition is no longer met. The condition is always tested after
     * each run. Therefore, the task will always run at least once before
     * ending the task. For example, <pre>
     *     int k = 10;
     *     () -&gt; k != 10
     * </pre>
     * In this case, the condition starts out as {@code false}, however, the
     * task will still execute once similar to a do-while loop.
     * <br>
     * This can easily be solved by checking the condition before scheduling
     * the task.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @param condition The {@link Supplier condition} that must be {@code true}
     *         in order for the task to continue to run.
     * @return The {@link BukkitTask task} that was scheduled.
     */
    public static BukkitTask repeatAsyncWhile(final Runnable run, final long delay,
            final long period, final Supplier<Boolean> condition) {
        final BukkitTask task = Scheduler.repeatAsync(run, delay, period);
        TASKS.add(new Task(false, task, condition));
        return task;
    }

    /**
     * Run a task repeatedly (every period of ticks) until the specified
     * amount of time has elapsed.
     * <p>
     * The task will always be executed at least once. For example, if the
     * duration to run is less than one tick ({@code unit.toMillis(duration)
     * < (50ms = 1 tick)}), the task will still be executed a single time
     * before being cancelled.
     * <p>
     * Timing precision is in milliseconds, however, the task is not guaranteed
     * to run a specific amount of time due to server lag.<pre>1000ms ≈ 20 ticks</pre>
     * If the duration given was 4 seconds and the task is supposed to run every
     * 5 ticks, then it should run about 16 times ({@code 20 / 5 * 4}), however,
     * this is not guaranteed and it can be any amount between 1 to 16 times
     * depending on server lag.
     * <br>
     * This would be guaranteed not to exceed 16 executions, though, since TPS
     * will never be above 20.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @param duration The amount of {@link TimeUnit units} to run for.
     * @param unit The {@link TimeUnit} to multiply the duration by.
     * @return The {@link BukkitTask task} that was scheduled.
     */
    public static BukkitTask repeatFor(final Runnable run, final long delay,
            final long period, final long duration, final TimeUnit unit) {
        final BukkitTask task = Scheduler.repeat(run, delay, period);
        TASKS.add(new Task(unit.toMillis(duration), task));
        return task;
    }

    /**
     * Run a task asynchronously repeatedly (every period of ticks) until
     * the specified amount of time has elapsed.
     * <p>
     * The task will always be executed at least once. For example, if the
     * duration to run is less than one tick ({@code unit.toMillis(duration)
     * < (50ms = 1 tick)}), the task will still be executed a single time
     * before being cancelled.
     * <p>
     * Timing precision is in milliseconds, however, the task is not guaranteed
     * to run a specific amount of time due to server lag.<pre>1000ms ≈ 20 ticks</pre>
     * If the duration given was 4 seconds and the task is supposed to run every
     * 5 ticks, then it should run about 16 times ({@code 20 / 5 * 4}), however,
     * this is not guaranteed and it can be any amount between 1 to 16 times
     * depending on server lag.
     * <br>
     * This would be guaranteed not to exceed 16 executions, though, since TPS
     * will never be above 20.
     *
     * @param run The {@link Runnable} task to execute every period.
     * @param delay The delay in ticks before the first run of the task.
     * @param period The period in ticks to wait until running again after each run.
     * @param duration The amount of {@link TimeUnit units} to run for.
     * @param unit The {@link TimeUnit} to multiply the duration by.
     * @return The {@link BukkitTask task} that was scheduled.
     */
    public static BukkitTask repeatAsyncFor(final Runnable run, final long delay,
            final long period, final long duration, final TimeUnit unit) {
        final BukkitTask task = Scheduler.repeatAsync(run, delay, period);
        TASKS.add(new Task(unit.toMillis(duration), task));
        return task;
    }

    /**
     * Execute the given {@link Runnable} asynchronously using
     * a {@link ExecutorService}. This method avoids the
     * {@link BukkitScheduler} and has precision in milliseconds
     * instead of ticks.
     *
     * @param runnable The runnable to execute.
     */
    public static void execute(final Runnable runnable) {
        EXECUTOR_SERVICE.execute(runnable);
    }

    /**
     * Schedule the given {@link Runnable} asynchronously with a delay.
     * This method avoids the {@link BukkitScheduler} and has precision
     * in milliseconds instead of ticks.
     *
     * @param runnable The runnable to execute.
     * @param delay The delay in {@link TimeUnit units} to delay the execution for.
     * @param unit The {@link TimeUnit unit} of delay to delay the execution for.
     * @return The {@link ScheduledFuture} to determine when the runnable is executed.
     */
    public static ScheduledFuture<?> later(final Runnable runnable, final long delay, final TimeUnit unit) {
        return EXECUTOR_SERVICE.schedule(runnable, delay, unit);
    }

    /**
     * Schedule the given {@link Runnable} at a fixed rate of execution.
     * This method avoids the {@link BukkitScheduler} and has precision
     * in milliseconds instead of ticks.
     *
     * @param runnable The runnable to execute every period.
     * @param period The period of {@link TimeUnit units} to wait between executions.
     * @param unit The {@link TimeUnit unit} of delay to measure the period in.
     * @return The {@link ScheduledFuture} to determine when the runnable has finished
     *         execution or to use to {@link ScheduledFuture#cancel(boolean) cancel} the task.
     */
    public static ScheduledFuture<?> repeat(final Runnable runnable, final long period, final TimeUnit unit) {
        return EXECUTOR_SERVICE.scheduleAtFixedRate(runnable, 0, period, unit);
    }

    private static class Task {

        private final long limit;
        private final boolean until;
        private final BukkitTask task;
        private final Supplier<Boolean> condition;

        private Task(final long limit, final BukkitTask task) {
            this.task = task;
            this.until = false;
            this.condition = null;
            this.limit = System.currentTimeMillis() + limit;
        }

        private Task(final boolean until, final BukkitTask task, final Supplier<Boolean> condition) {
            this.limit = -1;
            this.until = until;
            this.task = task;
            this.condition = condition;
        }

        private boolean cancel() {

            if (this.limit != -1) {
                return System.currentTimeMillis() >= this.limit;
            }

            if (this.until) {

                if (this.condition.get()) {
                    this.task.cancel();
                    return true;
                }

                return false;
            }

            if (!this.condition.get()) {
                this.task.cancel();
                return true;
            }

            return false;
        }

        @Override
        public boolean equals(final Object obj) {
            return obj != null && obj.getClass() == Task.class &&
                   this.task.getTaskId() == ((Task) obj).task.getTaskId();
        }
    }
}
