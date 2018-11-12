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

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class Strings {

    // CENTER_CHAT and INVENTORY_LENGTH are both only half of
    // of the actual length of each of them
    private static final int CENTER_CHAT = 160, INVENTORY_LENGTH = 78, LORE_LENGTH = 240, BOOK_LENGTH = 108;

    /**
     * Limit the given string to a maximum width of the given amount.
     * This is the measure according to the {@link #getWidth(String)}
     * method. This method is useful for dynamic string limiting where
     * the width is required to be at a certain point visually and go
     * no further not independent of the character widths.
     * <p>
     * This is <i>not</i> a method to limit the characters in a string.
     * A code sequence such as the following can be used to limit character
     * lengths:
     * <pre>if (str.length() &gt; length)
     *  str.substring(0, length);</pre>
     *
     * @param str The string to limit.
     * @param width The maximum width the string should have.
     * @return The string that is limited if it was needed.
     */
    public static String limit(String str, int width) {

        if (Strings.getWidth(str) <= width) {
            return str;
        }

        int current = 0;
        boolean isBold = false, previousCode = false;
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {

            if (c == ChatColor.COLOR_CHAR) {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                FontCharacter fontChar = FontCharacter.getByCharacter(c);
                current += fontChar != null ? isBold ? fontChar.getBold() : fontChar.getLength() : 5;
            }

            if (current <= width) {
                sb.append(c);
            } else {
                break;
            }
        }

        return sb.toString();
    }

    /**
     * Split the given string into different lines so it
     * looks nice in the lore of an item and doesn't take
     * up too much room.
     * <p>
     * Basic formatting can be achieved through using and
     * %n for new lines.
     *
     * @param str The string to split for lore.
     * @param linePrefix The color(s) that should prefix each line.
     * @return The different lines for the lore.
     */
    public static List<String> splitForLore(String str, ChatColor... linePrefix) {

        String prefix = linePrefix.length == 0 ? ChatColor.WHITE.toString() : StringUtils.join(linePrefix);
        String[] strings = StringUtils.split(str, "%n");
        List<String> lines = new LinkedList<>();
        for (String string : strings) {

            int length = 0;
            boolean isBold = false, previousCode = false;
            for (int i = 0; i < string.length(); i++) {

                char c = string.charAt(i);
                if (c == ChatColor.COLOR_CHAR) {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                }
                // If we found a space and the length if greater than our max length
                else if (c == ' ' && length > LORE_LENGTH) {
                    // Cut off the portion we're at and add it
                    lines.add(prefix + string.substring(0, i));
                    // Then cut it off the string (along with the space)
                    string = string.substring(i + 1);
                    // Then reset everything to start at the beginning
                    // of the new string
                    length = 0;
                    i = 0;
                } else {
                    FontCharacter fontChar = FontCharacter.getByCharacter(c);
                    length += fontChar != null ? isBold ? fontChar.getBold() : fontChar.getLength() : 5;
                }
            }

            // Add any left overs to the string
            lines.add(prefix + string);
        }

        return lines;
    }

    /**
     * Split the given string into different lines so that it
     * fits on the page of a book.
     * <p>
     * A single string with proper line break characters will
     * be input into the string at the correct points.
     *
     * @param str The string to split for the book.
     * @param center If each of the lines should be centered.
     * @param color The color to place before each line.
     * @return The text for the book with line breaks.
     */
    public static String splitForBook(String str, boolean center, ChatColor color) {

        String[] strings = StringUtils.split(str, '\n');
        List<String> lines = new LinkedList<>();
        for (String string : strings) {

            int length = 0, lastSpace = -1;
            boolean isBold = false, previousCode = false;
            for (int i = 0; i < string.length(); i++) {

                char c = string.charAt(i);
                if (c == ' ') {
                    lastSpace = i;
                }

                if (c == ChatColor.COLOR_CHAR) {
                    previousCode = true;
                } else if (previousCode) {
                    previousCode = false;
                    isBold = c == 'l' || c == 'L';
                } else {
                    FontCharacter craftChar = FontCharacter.getByCharacter(c);
                    int addition = craftChar != null ? isBold ? craftChar.getBold() : craftChar.getLength() : 5;
                    length += addition;
                }

                if (length >= BOOK_LENGTH && lastSpace != -1) {
                    lines.add(string.substring(0, lastSpace));
                    string = lastSpace + 1 < string.length() ? string.substring(lastSpace + 1) : "";
                    // Then reset everything to start at the beginning
                    // of the new string
                    lastSpace = -1;
                    length = 0;
                    i = 0;
                }
            }

            // Add any left overs to the string
            lines.add(string);
        }

        if (center) {

            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, color + Strings.centerBookLine(lines.get(i)));
            }
        }

        return StringUtils.join(lines, '\n');
    }

    /**
     * Center the given message for chat placing the required
     * amount of spaces before it to make it centered.
     *
     * @param msg The message to center.
     * @return The centered message.
     */
    public static String centerMessage(String msg) {

        if (msg == null || msg.isEmpty()) {
            return msg;
        }

        int length = Strings.getWidth(msg);
        int neededSpace = CENTER_CHAT - length / 2;
        StringBuilder spaces = new StringBuilder();
        for (int spaced = 0; spaced < neededSpace; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(msg).toString();
    }

    /**
     * Center the title of an inventory on the top of the
     * GUI screen.
     *
     * @param title The title to center.
     * @return The centered title.
     */
    public static String centerTitle(String title) {

        if (title == null || title.isEmpty()) {
            return title;
        }

        if (title.length() >= 32) {
            return title.substring(32);
        }

        int length = Strings.getWidth(title);
        StringBuilder spaces = new StringBuilder();
        int neededSpace = INVENTORY_LENGTH - length / 2;
        for (int spaced = 0; spaced < neededSpace; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(title).toString();
    }

    /**
     * Center a line of text in a book.
     *
     * @param line The line of text to center
     * @return The line, but centered for a book.
     */
    public static String centerBookLine(String line) {

        if (line == null || line.isEmpty()) {
            return line;
        }

        int length = Strings.getWidth(line);
        if (length == 0) {
            return line;
        }

        StringBuilder spaces = new StringBuilder();
        int neededSpace = BOOK_LENGTH / 2 - length / 2;
        for (int spaced = 0; spaced < neededSpace; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(line).toString();
    }

    /**
     * Center the given string given a base that represents
     * the longest string to center off of.
     *
     * @param str The string to center.
     * @param base The base length to center off of.
     * @return The centered form of the string.
     */
    public static String centerWithBase(String str, int base) {

        if (str == null || str.isEmpty()) {
            return str;
        }

        int length = Strings.getWidth(str);
        StringBuilder spaces = new StringBuilder();
        int neededSpaced = (base - length) / 2;
        for (int spaced = 0; spaced < neededSpaced; spaced += FontCharacter.SPACE.getLength()) {
            spaces.append(' ');
        }

        return spaces.append(str).toString();
    }

    /**
     * Center the given lines of text based on the longest
     * line in the text.
     *
     * @param lines The lines to center.
     * @return The centered text.
     */
    public static List<String> centerLines(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return lines;
        }

        int fullSize = Strings.getLongestLine(lines);
        List<String> newLines = new ArrayList<>(lines.size());
        lines.forEach(line -> {

            int length = Strings.getWidth(line);
            int neededSpace = (fullSize - length) / 2;
            StringBuilder spaces = new StringBuilder();
            for (int spaced = 0; spaced < neededSpace; spaced += FontCharacter.SPACE.getLength()) {
                spaces.append(' ');
            }

            newLines.add(spaces.append(line).toString());
        });

        return newLines;
    }

    /**
     * Get the line with the longest total length in
     * the list of lines given.
     *
     * @param lines The lines of text to get the longest of.
     * @return The length of the longest line.
     */
    public static int getLongestLine(List<String> lines) {

        if (lines == null || lines.isEmpty()) {
            return 0;
        }

        int longest = 0;
        for (String line : lines) {

            int length = Strings.getWidth(line);
            if (longest < length) {
                longest = length;
            }
        }

        return longest;
    }

    /**
     * Get the width of the given string where each letter
     * is based off of the widths given in it's respective
     * {@link FontCharacter} character.
     *
     * @param str The string to get the length of.
     * @return The length of the string.
     */
    public static int getWidth(String str) {

        int length = 0;
        boolean isBold = false, previousCode = false;
        for (char c : str.toCharArray()) {

            if (c == ChatColor.COLOR_CHAR) {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                FontCharacter fontChar = FontCharacter.getByCharacter(c);
                length += fontChar != null ? isBold ? fontChar.getBold() : fontChar.getLength() : 5;
            }
        }

        return length;
    }
}
