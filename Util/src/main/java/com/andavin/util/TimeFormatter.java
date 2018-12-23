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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @since December 13, 2018
 * @author Andavin
 */
public final class TimeFormatter {

    private static final EnumSet<TimeUnit> TIME_UNITS = EnumSet.range(TimeUnit.YEAR, TimeUnit.SECOND);
    private static final long[] UNITS = TIME_UNITS.stream().mapToLong(TimeUnit::getMilliseconds).toArray();
    private static final DateFormat SIMPLE_FORMAT = new SimpleDateFormat("MMMMM d, yyyy");
    private static final DateFormat FORMAT = new SimpleDateFormat("MMMMM d, yyyy 'at' h:mm a");
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?:([0-9]+)\\s*ye?a?r?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*mon?t?h?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*we?e?k?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*da?y?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*ho?u?r?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:mill?i?s?e?c?o?n?d?s?|ms)[,\\s]*)?" +
                    "(?:([0-9]+)\\s*mi?n?u?t?e?s?[,\\s]*)?" +
                    "(?:([0-9]+)\\s*(?:se?c?o?n?d?s?)?)?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Parse a string of time. The string should be in the relative format of
     * {@code [0-9] identifier} where {@code identifier} is the unit of time.
     * There can be multiple formats in the string and an undefined amount or
     * type of spacing.
     * <p>
     * <b>Warning</b>: It is always better for the string to be formatted with the
     * start being the highest unit of time and working it's way down to the
     * lowest (years, then months, then weeks etc.). If it is not in this order
     * the search algorithm may mix up units and some units may end up being incorrect.
     *
     * @param timeFormat The string of time to parse for {@link TimeUnit}s.
     * @return The amount of time in milliseconds that was parsed from the string.
     * @throws NumberFormatException If the string contained elements other than the numbers and identifiers.
     */
    public static long parse(String timeFormat) throws NumberFormatException {

        long total = 0;
        boolean found = false;
        Matcher matcher = TIME_PATTERN.matcher(timeFormat);
        while (matcher.find()) {

            String main = matcher.group();
            if (main == null || main.isEmpty()) {
                continue;
            }

            int length = UNITS.length;
            for (int i = 0; i <= length; i++) {

                String group = matcher.group(i + 1);
                if (group == null || group.isEmpty()) {
                    continue;
                }

                found = true;
                if (i < 5) {
                    total += Integer.parseInt(group) * UNITS[i];
                } else if (i == 5) {
                    total += Long.parseLong(group);
                } else {
                    total += Integer.parseInt(group) * UNITS[i - 1];
                }
            }
        }

        if (!found && !timeFormat.isEmpty()) {
            throw new NumberFormatException("§f" + timeFormat + "§c is not a valid time format.\n" +
                    "§eSomething like §f2h5m36s§e is §f2 hours 5 minutes 36 seconds§e.");
        }

        return total;
    }

    /**
     * Get the given date formatted into a friendly
     * calendar date (e.g. November 2, 2017 at 3:05 PM).
     *
     * @param date The unix date to format.
     * @return The formatted date string.
     */
    public static String formatDate(long date) {
        return FORMAT.format(date);
    }

    /**
     * Get the given date formatted into a friendly calendar date (e.g. November 2, 2017 at 3:05 PM).
     *
     * @param date The unix date to format.
     * @param simple If the date should show hour, minute and AM-PM info.
     * @return The formatted date string.
     */
    public static String formatDate(long date, boolean simple) {
        return simple ? SIMPLE_FORMAT.format(date) : FORMAT.format(date);
    }

    /**
     * Format the given amount of time for all {@link TimeUnit}s
     * except for {@link TimeUnit#MILLISECOND}. This will default
     * to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, formatting 65,000 milliseconds would be returned
     * as {@code 1m 5s} for 1 minute 5 seconds.
     *
     * @param time The time to format (in milliseconds).
     * @return The formatted version of the time given.
     */
    public static String format(long time) {
        return formatDifference(0, time, true, TIME_UNITS);
    }

    /**
     * Format the given amount of time for all {@link TimeUnit}s
     * except for {@link TimeUnit#MILLISECOND}.
     * <p>
     * For example, formatting 65,000 milliseconds would be returned
     * as {@code 1m 5s} when {@code abbreviate} if true or {@code 1 minute
     * 5 seconds} when it is false.
     *
     * @param time The time to format (in milliseconds).
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @return The formatted version of the time given.
     */
    public static String format(long time, boolean abbreviate) {
        return formatDifference(0, time, abbreviate, TIME_UNITS);
    }

    /**
     * Format the given amount of time for each of the {@link TimeUnit}s
     * specified. This will default to abbreviating the suffixes for
     * each unit of time.
     * <p>
     * For example, formatting 65,000 milliseconds with {@link TimeUnit#MINUTE}
     * and {@link TimeUnit#SECOND} would return as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link TimeUnit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     *
     * @param time The time to format (in milliseconds).
     * @param unit The first {@link TimeUnit} to include.
     * @param units Any extra {@link TimeUnit}s to include.
     * @return The formatted version of the time given.
     */
    public static String format(long time, TimeUnit unit, TimeUnit... units) {
        return formatDifference(0, time, true, EnumSet.of(unit, units));
    }

    /**
     * Format the given amount of time for each of the {@link TimeUnit}s specified.
     * <p>
     * For example, formatting 65,000 milliseconds with {@link TimeUnit#MINUTE}
     * and {@link TimeUnit#SECOND} would return as {@code 1m 5s} for 1 minute 5 seconds.
     *
     * @param time The time to format (in milliseconds).
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param unit The first {@link TimeUnit} to include.
     * @param units Any extra {@link TimeUnit}s to include.
     * @return The formatted version of the time given.
     */
    public static String format(long time, boolean abbreviate, TimeUnit unit, TimeUnit... units) {
        return formatDifference(0, time, abbreviate, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for all {@link TimeUnit}s except for {@link TimeUnit#MILLISECOND}.
     * This will default to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to) {
        return formatDifference(System.currentTimeMillis(), to, true, TIME_UNITS);
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for all {@link TimeUnit}s except for {@link TimeUnit#MILLISECOND}.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, boolean abbreviate) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, TIME_UNITS);
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for each of the {@link TimeUnit}s specified.
     * This will default to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link TimeUnit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param unit The first {@link TimeUnit} to include.
     * @param units Any extra {@link TimeUnit}s to include.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, TimeUnit unit, TimeUnit... units) {
        return formatDifference(System.currentTimeMillis(), to, true, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for each of the {@link TimeUnit}s specified.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link TimeUnit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param unit The first {@link TimeUnit} to include.
     * @param units Any extra {@link TimeUnit}s to include.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, boolean abbreviate, TimeUnit unit, TimeUnit... units) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between the two times specified
     * for all {@link TimeUnit}s except for {@link TimeUnit#MILLISECOND}.
     * This will default to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to) {
        return formatDifference(from, to, true, TIME_UNITS);
    }

    /**
     * Format the difference in time between the two times specified
     * for all {@link TimeUnit}s except for {@link TimeUnit#MILLISECOND}.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to, boolean abbreviate) {
        return formatDifference(from, to, abbreviate, TIME_UNITS);
    }

    /**
     * Format the difference in time between the two times specified
     * for each of the {@link TimeUnit}s specified. This will default
     * to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param unit The first {@link TimeUnit} to include.
     * @param units Any extra {@link TimeUnit}s to include.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to, TimeUnit unit, TimeUnit... units) {
        return formatDifference(from, to, true, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between the two times specified
     * for each of the {@link TimeUnit}s specified.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param unit The first {@link TimeUnit} to include.
     * @param units Any extra {@link TimeUnit}s to include.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to, boolean abbreviate, TimeUnit unit, TimeUnit... units) {
        return formatDifference(from, to, abbreviate, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between the two times specified
     * for each of the {@link TimeUnit}s specified.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param units All of the {@link TimeUnit}s to format for (cannot be empty).
     * @return The formatted version of the difference between the specified times.
     * @throws IllegalArgumentException If there are no {@link TimeUnit}s contained
     *                                  within the {@link EnumSet}.
     */
    public static String formatDifference(long from, long to, boolean abbreviate, EnumSet<TimeUnit> units) throws IllegalArgumentException {

        checkArgument(!units.isEmpty(), "requires at least one TimeUnit");
        // Ensure from is always <= to
        if (from > to) {
            long temp = from;
            from = to;
            to = temp;
        }

        TimeUnit lowest = null;
        long difference = to - from;
        StringBuilder sb = new StringBuilder(units.size() * (abbreviate ? 15 : 10)); // Estimate size
        for (TimeUnit unit : units) {

            lowest = unit; // Will always be the last iteration
            long ms = unit.getMilliseconds();
            long found = difference / ms;
            if (found > 0) {
                difference -= found * ms;
                sb.append(found).append(unit.getSuffix(abbreviate, found == 1)).append(' ');
            }
        }

        if (difference == to - from) { // Nothing was formatted
            return sb.append('0').append(lowest.getSuffix(abbreviate, false)).toString();
        }

        // Cut off the extra space at the end
        return sb.substring(0, sb.length() - 1);
    }

    public enum TimeUnit {

        // Greatest to least for iteration order
        YEAR(1000L * 60 * 60 * 24 * 365, " year", "y"),
        MONTH(1000L * 60 * 60 * 24 * 30, " month", "mo"),
        WEEK(1000L * 60 * 60 * 24 * 7, " week", "w"),
        DAY(1000L * 60 * 60 * 24, " day", "d"),
        HOUR(1000L * 60 * 60, " hour", "h"),
        MINUTE(1000L * 60, " minute", "m"),
        SECOND(1000L, " second", "s"),
        MILLISECOND(1, " millisecond", "ms");

        private final long milliseconds;
        private final String singular, plural, abbreviated;

        TimeUnit(long milliseconds, String singular, String abbreviated) {
            this.milliseconds = milliseconds;
            this.singular = singular;
            this.plural = singular + 's'; // All just add an s
            this.abbreviated = abbreviated;
        }

        /**
         * Get how many milliseconds that this unit of time
         * contains (e.g. 86,400,000 milliseconds in {@link #DAY}).
         *
         * @return The amount of milliseconds in this unit of time.
         */
        public long getMilliseconds() {
            return milliseconds;
        }

        /**
         * Get the suffix for this unit of time based on the
         * given parameters.
         *
         * @param abbreviate If the suffix should be abbreviated such as
         *                   {@code s} instead of {@code seconds}.
         * @param singular If the suffix should be the singular
         *                 version such as {@code day} instead of {@code days}.
         * @return The suffix for this unit of time.
         */
        public String getSuffix(boolean abbreviate, boolean singular) {
            return abbreviate ? this.abbreviated : singular ? this.singular : this.plural;
        }
    }
}
