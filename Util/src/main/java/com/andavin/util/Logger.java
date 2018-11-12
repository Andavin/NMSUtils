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

import java.util.logging.Level;

/**
 * A custom singleton logger class that wraps a logger from
 * a single plugin and makes it more friendly to use.
 * <p>
 * This automatically detects the plugin from which the logger
 * is called and logs under that plugin's logger.
 *
 * @author Andavin
 */
public final class Logger {

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void info(Object obj) {
        PluginRegistry.getLogger().log(Level.INFO, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static synchronized void info(String msg) {
        PluginRegistry.getLogger().log(Level.INFO, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void info(String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.INFO, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void info(Throwable throwable, String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.INFO, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void info(Throwable throwable) {
        PluginRegistry.getLogger().log(Level.INFO, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void warn(Object obj) {
        PluginRegistry.getLogger().log(Level.WARNING, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static synchronized void warn(String msg) {
        PluginRegistry.getLogger().log(Level.WARNING, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void warn(String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.WARNING, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void warn(Throwable throwable, String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.WARNING, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void warn(Throwable throwable) {
        PluginRegistry.getLogger().log(Level.WARNING, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void severe(Object obj) {
        PluginRegistry.getLogger().log(Level.SEVERE, obj.toString());
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void severe(String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.SEVERE, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void severe(Throwable throwable, String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.SEVERE, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void severe(Throwable throwable) {
        PluginRegistry.getLogger().log(Level.SEVERE, throwable.getMessage(), throwable);
    }

    /**
     * Log the value of <code>object.toString()</code>.
     *
     * @param obj The object to log.
     */
    public static synchronized void debug(Object obj) {
        PluginRegistry.getLogger().log(Level.CONFIG, obj.toString());
    }

    /**
     * Log the given message.
     *
     * @param msg The message to log.
     */
    public static synchronized void debug(String msg) {
        PluginRegistry.getLogger().log(Level.CONFIG, msg);
    }

    /**
     * Log the given message replacing the <code>"{}"</code> placeholders
     * with the given arguments.
     *
     * @param msg The message to log.
     * @param args The arguments to replace placeholders with.
     */
    public static synchronized void debug(String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.CONFIG, Logger.format(msg, args));
    }

    /**
     * Log the given throwable and a message replacing the <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param throwable The throwable to log.
     * @param msg The message to log with it.
     * @param args The arguments to place into the message.
     */
    public static synchronized void debug(Throwable throwable, String msg, Object... args) {
        PluginRegistry.getLogger().log(Level.CONFIG, Logger.format(msg, args), throwable);
    }

    /**
     * Log the given throwable.
     *
     * @param throwable The throwable to log.
     */
    public static synchronized void debug(Throwable throwable) {
        PluginRegistry.getLogger().log(Level.CONFIG, throwable.getMessage(), throwable);
    }

    /**
     * Method from TinyLogger logging framework. Replace <code>"{}"</code>
     * placeholders with the given arguments.
     *
     * @param message The message to place arguments into.
     * @param arguments The arguments to place into the message.
     * @return The message that has been formatted.
     */
    private static String format(String message, Object... arguments) {

        if (arguments == null || arguments.length == 0) {
            return message;
        }

        int start = 0, argumentIndex = 0, openBraces = 0;
        StringBuilder builder = new StringBuilder(message.length() + arguments.length * 16);
        for (int index = 0; index < message.length(); ++index) {

            char character = message.charAt(index);
            if (character == '{') {

                if (openBraces++ == 0 && start < index) {
                    builder.append(message, start, index);
                    start = index;
                }
            } else if (character == '}' && openBraces > 0) {

                if (--openBraces == 0) {

                    if (argumentIndex < arguments.length) {

                        Object argument = arguments[argumentIndex++];
                        if (index == start + 1) {
                            builder.append(argument);
                        } else {
                            builder.append(format(message.substring(start + 1, index), argument));
                        }
                    } else {
                        builder.append(message, start, index + 1);
                    }

                    start = index + 1;
                }
            }
        }

        if (start < message.length()) {
            builder.append(message, start, message.length());
        }

        return builder.toString();
    }
}
