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

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.ChatColor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.andavin.reflect.Reflection.*;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author Andavin
 * @since February 03, 2018
 */
public class ChatComponent {

    private static final Field CHAT, MESSAGE, SIBLINGS;
    private static final Method ADD_SIBLING;
    private static final Method FROM_STRING = findMethod(findCraftClass(
            "util.CraftChatMessage"), "fromString", String.class);
    private static final Map<ChatColor, Object> NMS_COLORS = new EnumMap<>(ChatColor.class);

    static {

        Class<?> format = findMcClass("EnumChatFormat");
        for (ChatColor color : ChatColor.values()) {
            NMS_COLORS.put(color, getFieldValue(format, null, color.name()));
        }

        Class<?> iBaseClass = findMcClass("IChatBaseComponent");
        Class<?> baseClass = findMcClass("ChatBaseComponent");
        Class<?> chatClass = findMcClass("ChatComponentText");
        Class<?> messageClass = findMcClass("ChatMessage");

        CHAT = findField(chatClass, "b");
        MESSAGE = findField(messageClass, "d");
        SIBLINGS = findField(baseClass, "a");
        ADD_SIBLING = findMethod(iBaseClass, "addSibling", iBaseClass);
        if (CHAT == null || MESSAGE == null) {
            throw new NullPointerException("Classes for ChatComponentText and/or ChatMessage could not be found!");
        }

        Field mods = findField(Field.class, "modifiers");
        setFieldValue(mods, CHAT, CHAT.getModifiers() & ~Modifier.FINAL);
        setFieldValue(mods, MESSAGE, MESSAGE.getModifiers() & ~Modifier.FINAL);
    }

    /**
     * Create a new ChatComponent from text creating an IChatBaseComponent.
     *
     * @param text Initial text to place in the ChatComponent.
     * @return The new ChatComponent for the text.
     */
    public static ChatComponent create(String text) {
        return new ChatComponent(text);
    }

    /**
     * Get a new ChatComponent object from the string. This method
     * will account for formatting made using section characters
     * and incorporate them into the new component.
     *
     * @param str The string to get the component from.
     * @return The new ChatComponent from the given string.
     */
    public static ChatComponent fromString(String str) {

        Object[] comps = invokeMethod(FROM_STRING, null, str);
        if (comps != null) {
            return ChatComponent.wrap(comps[0]);
        }

        throw new NullPointerException("Could not retrieve component from string due to method error");
    }

    /**
     * Wrap the given IChatBaseComponent object into a ChatComponent.
     *
     * @param baseComponent The IChatBaseComponent to wrap.
     * @return The wrapper ChatComponent.
     */
    public static ChatComponent wrap(Object baseComponent) {
        return new ChatComponent(baseComponent);
    }

    private final Object base;
    private ChatComponent current;
    private final List<Object> siblings;

    private ChatComponent(String text) {
        this.base = null;
//        this.base = new ChatComponentText(text);
        this.siblings = getFieldValue(SIBLINGS, this.base);
    }

    private ChatComponent(Object baseComponent) {
        this.base = baseComponent;
        this.siblings = getFieldValue(SIBLINGS, this.base);
    }

    /**
     * Add a new addition to this component with
     * all of it's own formatting attributes.
     *
     * @param text The message to add to the end.
     * @return A reference to the new ChatComponent sibling.
     */
    public ChatComponent addSibling(String text) {
        return this.addSibling(new ChatComponent(text));
    }

    /**
     * Add a new addition to this component with
     * all of it's own formatting attributes.
     *
     * @param component The message to add to the end.
     * @return A reference to the new ChatComponent sibling.
     */
    public ChatComponent addSibling(ChatComponent component) {
        invokeMethod(ADD_SIBLING, this.base, component.base);
        this.current = component;
        return component;
    }

    /**
     * Add a new addition to this component with
     * all of it's own formatting attributes.
     *
     * @param baseComponent The message to add to the end.
     * @return A reference to the new ChatComponent sibling.
     */
    public ChatComponent addSibling(Object baseComponent) {
        return this.addSibling(new ChatComponent(baseComponent));
    }

    /**
     * Highlight words or phrases by retrieving these and
     * editing them inside of a {@link Highlighter} object.
     * This allows edits to all highlighted words at the same.
     * <br><b>Important:</b> Make sure to read the {@link #highlight(String, boolean)}
     * documentation as well to fully understand how a highlighter functions.
     *
     * @param highlight The words or phrases to retrieve all occurrences of.
     * @param caseSensitive Whether the word to be highlighted should be case sensitively chosen.
     * @return A new Highlighter object for easy editing of highlighted words.
     * @see #highlight(String, boolean)
     * @see Highlighter
     */
    public Highlighter highlight(boolean caseSensitive, String... highlight) {

        List<ChatComponent> comps = new ArrayList<>();
        for (String word : highlight) {

            List<ChatComponent> highlights = this.highlight(word, caseSensitive);
            if (!highlights.isEmpty()) {
                comps.addAll(highlights);
            }
        }

        return new Highlighter(comps);
    }

    /**
     * Highlight a word or phrase within this ChatComponent or its siblings.
     * This is done by retrieving the word or phrase and setting it's
     * attributes. Note that this method will <b>not</b> highlight the word
     * or phrase, but will retrieve the ChatComponent for the word or phrase
     * within this ChatComponent so that it can have its attributes set
     * independently from the rest of the component.
     * <p>
     * For example, if I have the phrase <code>"This is a big party. I love this party."</code>
     * and I want to highlight all of the occurrences of <code>"party"</code>.
     * This method will create independent components for the two
     * occurrences of that word. Therefore, this one component would
     * then be split into five separate components:
     * <ol>
     * <li><code>"This is a big "</code></li>
     * <li><code>"party"</code></li>
     * <li><code>". I love this "</code></li>
     * <li><code>"party"</code></li>
     * <li><code>"."</code></li>
     * </ol>
     * Out of these five, the two components containing the word <code>"party"</code>
     * will be returned as an array. They can then be edited with whatever
     * chat modifiers are desired.
     * <p>
     * The power of this is that once the chat modifiers are set you do not need
     * to replace any IChatBaseComponents in a packet or anywhere because the edits
     * that are made to the ChatComponent class are also made to the IChatBaseComponent.
     * <p>
     * In that light, the following code would actually edit an outgoing packet without
     * having to reset any values:
     * <pre>
     *     public void packetListener(PacketPlayOutChat chatPacket) {
     *
     *         ChatComponent component = new ChatComponent(chatPacket.getIChatBaseComponent());
     *         ChatComponent[] highlights = component.highlight("AWordOrPhrase");
     *         for(ChatComponent highlight : highlights) {
     *             highlight.setBold(true)
     *             .color(ChatColor.BLUE);
     *         }
     *     }
     * </pre>
     * All occurrences of the phrase <code>"AWordOrPhrase"</code> would then show up in chat with
     * the bold style and a chat color of blue.
     *
     * @param highlight The word or words to retrieve for highlighting.
     * @param caseSensitive Whether the highlighted word should be searched with case sensitivity.
     * @return All of the occurrences of the highlight word as ChatComponents so their attributes can be set.
     */
    public List<ChatComponent> highlight(String highlight, boolean caseSensitive) {

        List<ChatComponent> highlights = new LinkedList<>();
        List<ChatComponent> comps = this.getSiblings();
        this.siblings.clear(); // Clear all the siblings of the base component
        comps.add(0, this);

//        for (int i = 0; i < comps.size(); ++i) {
//
//            ChatComponent comp = comps.get(i);
//            String text = comp.getText();
//            int index = caseSensitive ? StringUtils.indexOf(text, highlight) :
//                    StringUtils.indexOfIgnoreCase(text, highlight);
//            if (index == -1) { // This component does not contain our word
//                continue;
//            }
//
//            comps.remove(i);
//            while (index != -1) {
//
//                ChatComponent begin = new ChatComponent(text.substring(0, index));  // Get the part before the word
//                begin.setChatModifier(comp.getChatModifier());                            // Set it to the same chat modifier that it was
//                comps.add(i++, begin);                                                    // Add it back in the same position
//
//                text = text.substring(index);
//                int wordEnd = highlight.length();
//
//                ChatComponent middle = new ChatComponent(text.substring(0, wordEnd));   // Get the word itself
//                highlights.add(middle);                                                       // No chat modifications
//                comps.add(i++, middle);                                                       // Add separate but after the beginning
//
//                if (!text.isEmpty()) {
//                    text = text.substring(wordEnd);                                           // Cut the word out of the string
//                }
//
//                index = caseSensitive ? StringUtils.indexOf(text, highlight) :
//                        StringUtils.indexOfIgnoreCase(text, highlight);                       // See if the end still contains the highlight
//            }
//
//            if (text.isEmpty()) {
//                continue;
//            }
//
//            ChatComponent end = new ChatComponent(text);
//            end.setChatModifier(comp.getChatModifier());                // Set the modifier to what it was
//            comps.add(i, end);                                          // Add it back in after the other two
//        }

        this.setText(comps.get(0).getText());  // Reset this chat component to the first index's text
        comps.remove(0);                 // Remove it from the list
        comps.forEach(this::addSibling);       // Add all the siblings onto this ChatComponent

        return highlights;
    }

    /**
     * Set the color of this ChatComponent.
     * Note that this cannot be a style only a color.
     *
     * @param color The color to set to.
     * @return A reference to this ChatComponent.
     * @throws IllegalArgumentException If the ChatColor is not a color.
     */
    public ChatComponent color(ChatColor color) {
        checkArgument(color.isColor(), "Color must be a color, but got a format %s instead.", color.name());
//        this.getChatModifier().setColor(this.getColor(color));
        return this;
    }

    /**
     * Get whether this ChatComponent has the bold
     * style attribute.
     *
     * @return Whether this ChatComponent is bold.
     */
    public boolean isBold() {
        return false;
//        return this.getChatModifier().isBold();
    }

    /**
     * Toggle this ChatComponent's bold style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent bold(boolean set) {
//        this.getChatModifier().setBold(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the italic
     * style attribute.
     *
     * @return Whether this ChatComponent is italic.
     */
    public boolean isItalic() {
        return false;
//        return this.getChatModifier().isItalic();
    }

    /**
     * Toggle this ChatComponent's italic style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent italic(boolean set) {
//        this.getChatModifier().setItalic(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the magic
     * style attribute.
     *
     * @return Whether this ChatComponent is magic.
     */
    public boolean isMagic() {
        return false;
//        return this.getChatModifier().isRandom();
    }

    /**
     * Toggle this ChatComponent's magic style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent magic(boolean set) {
//        this.getChatModifier().setRandom(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the strikethrough
     * style attribute.
     *
     * @return Whether this ChatComponent is strikethrough.
     */
    public boolean isStrikethrough() {
        return false;
//        return this.getChatModifier().isStrikethrough();
    }

    /**
     * Toggle this ChatComponent's strikethrough style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent strikethrough(boolean set) {
//        this.getChatModifier().setStrikethrough(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the underline
     * style attribute.
     *
     * @return Whether this ChatComponent is underlined.
     */
    public boolean isUnderlined() {
//        return this.getChatModifier().isUnderlined();
        return false;
    }

    /**
     * Toggle this ChatComponent's underline style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent underlined(boolean set) {
//        this.getChatModifier().setUnderline(set);
        return this;
    }

    /**
     * Set this component so that the text is able to be clicked
     * and perform an action.
     * <p>
     * <b>Important:</b> You must avoid using any unicode or symbols
     * that are not allowed in Minecraft chat in this method click
     * string (such as the section symbol §). If used Minecraft will
     * throw an exception on use of the click action.
     *
     * @param action The action to perform.
     * @param click The command to run, file to be opened, etc.
     * @return A reference to this object.
     */
    public ChatComponent event(ClickEvent.Action action, String click) {
//        final ChatModifier modifier = this.base.getChatModifier();
//        modifier.setChatClickable(new ChatClickable(this.getAction(action), click));
        return this;
    }

    /**
     * Set this component so that when it is hovered over
     * something will show up. This is also known as a tooltip.
     *
     * @param action What kind thing to show when hovered over.
     * @param hover What to show when hovered over.
     * @return A reference to this object.
     */
    public ChatComponent event(HoverEvent.Action action, ChatComponent hover) {
//        final ChatModifier modifier = this.base.getChatModifier();
//        modifier.setChatHoverable(new ChatHoverable(this.getAction(action), hover.getBaseComponent()));
        return this;
    }

    /**
     * Get all the siblings of this ChatComponent. If this
     * ChatComponent has no siblings this will return an empty list.
     *
     * @return A list of all this ChatComponent's siblings.
     */
    public List<ChatComponent> getSiblings() {
//        final List<IChatBaseComponent> baseComps = this.base.a();
//        final List<ChatComponent> siblings = new ArrayList<>(baseComps.size());
//        baseComps.forEach(base -> siblings.add(new Component_v1_8_R3(base)));
        return null;
    }

    /**
     * Get the text for this ChatComponent, but without
     * any formatting except that which is embedded in the text.
     *
     * @return The text for this ChatComponent.
     */
    public String getText() {
        return "";
//        return ((IChatBaseComponent) this.base).getText();
    }

    /**
     * Get the IChatBaseComponent that this ChatComponent
     * object is wrapping.
     *
     * @return The IChatBaseComponent of this object.
     */
    public Object getBaseComponent() {
        return this.base;
    }

    @Override
    public final boolean equals(Object o) {
        return o == this || o instanceof ChatComponent &&
                ((ChatComponent) o).getBaseComponent().equals(this.getBaseComponent());
    }

    @Override
    public final int hashCode() {
        return this.getBaseComponent().hashCode();
    }

    @Override
    public String toString() {
//        return CraftChatMessage.fromComponent(this.base);
        return null;
    }

    private void setText(String text) {

//        if (this.base instanceof ChatComponentText) {
//            setValue(CHAT, this.base, text);
//        } else {
//            setValue(MESSAGE, this.base, text);
//        }
    }

//    @Nonnull
//    private ChatModifier getChatModifier() {
//
////        final IChatBaseComponent base = this.current != null ? this.current.getBaseComponent() : this.base;
////        ChatModifier modifier = base.getChatModifier();
////        if (modifier == null) {
////            modifier = new ChatModifier();
////            base.setChatModifier(modifier);
////        }
////
////        return modifier;
//        return null;
//    }
//
//    private void setChatModifier(ChatModifier modifier) {
////        final IChatBaseComponent base = this.current != null ? this.current.getBaseComponent() : this.base;
////        base.setChatModifier(modifier);
//    }
//
//    @Nonnull
//    private EnumClickAction getAction(ClickEvent.Action action) {
//
//        switch (action) {
//
//            case RUN_COMMAND:
//                return EnumClickAction.RUN_COMMAND;
//
//            case SUGGEST_COMMAND:
//                return EnumClickAction.SUGGEST_COMMAND;
//
//            case OPEN_URL:
//                return EnumClickAction.OPEN_URL;
//
//            case OPEN_FILE:
//                return EnumClickAction.OPEN_FILE;
//
//            default:
//                return EnumClickAction.CHANGE_PAGE;
//        }
//    }
//
//    @Nonnull
//    private EnumHoverAction getAction(HoverEvent.Action action) {
//
//        switch (action) {
//
//            case SHOW_TEXT:
//                return EnumHoverAction.SHOW_TEXT;
//
//            case SHOW_ITEM:
//                return EnumHoverAction.SHOW_ITEM;
//
//            case SHOW_ACHIEVEMENT:
////                return EnumHoverAction.SHOW_ACHIEVEMENT;
//
//            case SHOW_ENTITY:
//                return EnumHoverAction.SHOW_ENTITY;
//        }
//
//        return null;
//    }
}
