package com.andavin.chat;

import com.andavin.reflect.Reflection;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.minecraft.server.v1_12_R1.ChatClickable.EnumClickAction;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.ChatHoverable.EnumHoverAction;
import net.minecraft.server.v1_12_R1.ChatModifier;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Andavin
 * @since February 03, 2018
 */
public class ChatComponent {

    private static final Field CHAT, MESSAGE, SIBLINGS;
    private static final Method ADD_SIBLING;
    private static final Method FROM_STRING = Reflection.getMethod(Reflection.getCraftClass(
            "util.CraftChatMessage"), "fromString", String.class);
    private static final Map<ChatColor, Object> NMS_COLORS = new EnumMap<>(ChatColor.class);
    static {

        final Class<?> format = Reflection.getMcClass("EnumChatFormat");
        for (final ChatColor color : ChatColor.values()) {
             NMS_COLORS.put(color, Reflection.getValue(format, null, color.name()));
        }

        final Class<?> iBaseClass = Reflection.getMcClass("IChatBaseComponent");
        final Class<?> baseClass = Reflection.getMcClass("ChatBaseComponent");
        final Class<?> chatClass = Reflection.getMcClass("ChatComponentText");
        final Class<?> messageClass = Reflection.getMcClass("ChatMessage");

        CHAT = Reflection.getField(chatClass, "b");
        MESSAGE = Reflection.getField(messageClass, "d");
        SIBLINGS = Reflection.getField(baseClass, "a");
        ADD_SIBLING = Reflection.getMethod(iBaseClass, "addSibling", iBaseClass);
        if (CHAT == null || MESSAGE == null) {
            throw new NullPointerException("Classes for ChatComponentText and/or ChatMessage could not be found!");
        }

        final Field mods = Reflection.getField(Field.class, "modifiers");
        Reflection.setValue(mods, CHAT, CHAT.getModifiers() & ~Modifier.FINAL);
        Reflection.setValue(mods, MESSAGE, MESSAGE.getModifiers() & ~Modifier.FINAL);
    }

    /**
     * Create a new ChatComponent from text creating an IChatBaseComponent.
     *
     * @param text Initial text to place in the ChatComponent.
     * @return The new ChatComponent for the text.
     */
    public static ChatComponent create(final String text) {
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
    public static ChatComponent fromString(final String str) {

        final Object[] comps = Reflection.invokeMethod(FROM_STRING, null, str);
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
    public static ChatComponent wrap(final Object baseComponent) {
        return new ChatComponent(baseComponent);
    }

    private final Object base;
    private ChatComponent current;
    private final List<Object> siblings;

    private ChatComponent(final String text) {
        this.base = new ChatComponentText(text);
        this.siblings = Reflection.getValue(SIBLINGS, this.base);
    }

    private ChatComponent(final Object baseComponent) {
        this.base = baseComponent;
        this.siblings = Reflection.getValue(SIBLINGS, this.base);
    }

    /**
     * Add a new addition to this component with
     * all of it's own formatting attributes.
     *
     * @param text The message to add to the end.
     * @return A reference to the new ChatComponent sibling.
     */
    public ChatComponent addSibling(final String text) {
        return this.addSibling(new ChatComponent(text));
    }

    /**
     * Add a new addition to this component with
     * all of it's own formatting attributes.
     *
     * @param component The message to add to the end.
     * @return A reference to the new ChatComponent sibling.
     */
    public ChatComponent addSibling(final ChatComponent component) {
        Reflection.invokeMethod(ADD_SIBLING, this.base, component.base);
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
    public ChatComponent addSibling(final Object baseComponent) {
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
    public Highlighter highlight(final boolean caseSensitive, final String... highlight) {

        final List<ChatComponent> comps = new ArrayList<>();
        for (final String word : highlight) {

            final List<ChatComponent> highlights = this.highlight(word, caseSensitive);
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
    public List<ChatComponent> highlight(final String highlight, final boolean caseSensitive) {


        final List<ChatComponent> highlights = new LinkedList<>();
        final List<ChatComponent> comps = this.getSiblings();
        this.siblings.clear(); // Clear all the siblings of the base component
        comps.add(0, this);

        for (int i = 0; i < comps.size(); ++i) {

            final ChatComponent comp = comps.get(i);
            String text = comp.getText();
            int index = caseSensitive ? StringUtils.indexOf(text, highlight) :
                    StringUtils.indexOfIgnoreCase(text, highlight);
            if (index == -1) { // This component does not contain our word
                continue;
            }

            comps.remove(i);
            while (index != -1) {

                final ChatComponent begin = new ChatComponent(text.substring(0, index));  // Get the part before the word
                begin.setChatModifier(comp.getChatModifier());                            // Set it to the same chat modifier that it was
                comps.add(i++, begin);                                                    // Add it back in the same position

                text = text.substring(index);
                final int wordEnd = highlight.length();

                final ChatComponent middle = new ChatComponent(text.substring(0, wordEnd));   // Get the word itself
                highlights.add(middle);                                                       // No chat modifications
                comps.add(i++, middle);                                                       // Add separate but after the beginning

                if (!text.isEmpty()) {
                    text = text.substring(wordEnd);                                           // Cut the word out of the string
                }

                index = caseSensitive ? StringUtils.indexOf(text, highlight) :
                        StringUtils.indexOfIgnoreCase(text, highlight);                       // See if the end still contains the highlight
            }

            if (text.isEmpty()) {
                continue;
            }

            final ChatComponent end = new ChatComponent(text);
            end.setChatModifier(comp.getChatModifier());                // Set the modifier to what it was
            comps.add(i, end);                                          // Add it back in after the other two
        }


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
    public ChatComponent color(final ChatColor color) {
        Preconditions.checkArgument(color.isColor(), "Color must be a color, but got a format %s instead.", color.name());
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
        return this.getChatModifier().isBold();
    }

    /**
     * Toggle this ChatComponent's bold style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent bold(final boolean set) {
        this.getChatModifier().setBold(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the italic
     * style attribute.
     *
     * @return Whether this ChatComponent is italic.
     */
    public boolean isItalic() {
        return this.getChatModifier().isItalic();
    }

    /**
     * Toggle this ChatComponent's italic style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent italic(final boolean set) {
        this.getChatModifier().setItalic(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the magic
     * style attribute.
     *
     * @return Whether this ChatComponent is magic.
     */
    public boolean isMagic() {
        return this.getChatModifier().isRandom();
    }

    /**
     * Toggle this ChatComponent's magic style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent magic(final boolean set) {
        this.getChatModifier().setRandom(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the strikethrough
     * style attribute.
     *
     * @return Whether this ChatComponent is strikethrough.
     */
    public boolean isStrikethrough() {
        return this.getChatModifier().isStrikethrough();
    }

    /**
     * Toggle this ChatComponent's strikethrough style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent strikethrough(final boolean set) {
        this.getChatModifier().setStrikethrough(set);
        return this;
    }

    /**
     * Get whether this ChatComponent has the underline
     * style attribute.
     *
     * @return Whether this ChatComponent is underlined.
     */
    public boolean isUnderlined() {
        return this.getChatModifier().isUnderlined();
    }

    /**
     * Toggle this ChatComponent's underline style to
     * on or off.
     *
     * @param set Whether it should be set on or off.
     * @return A reference to this ChatComponent.
     */
    public ChatComponent underlined(final boolean set) {
        this.getChatModifier().setUnderline(set);
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
    public ChatComponent event(final ClickEvent.Action action, final String click) {
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
    public ChatComponent event(final HoverEvent.Action action, final ChatComponent hover) {
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
        return ((IChatBaseComponent) this.base).getText();
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
    public final boolean equals(final Object o) {
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

    private void setText(final String text) {

        if (this.base instanceof ChatComponentText) {
            Reflection.setValue(CHAT, this.base, text);
        } else {
            Reflection.setValue(MESSAGE, this.base, text);
        }
    }

    @Nonnull
    private ChatModifier getChatModifier() {

//        final IChatBaseComponent base = this.current != null ? this.current.getBaseComponent() : this.base;
//        ChatModifier modifier = base.getChatModifier();
//        if (modifier == null) {
//            modifier = new ChatModifier();
//            base.setChatModifier(modifier);
//        }
//
//        return modifier;
        return null;
    }

    private void setChatModifier(final ChatModifier modifier) {
//        final IChatBaseComponent base = this.current != null ? this.current.getBaseComponent() : this.base;
//        base.setChatModifier(modifier);
    }

    @Nonnull
    private EnumClickAction getAction(final ClickEvent.Action action) {

        switch (action) {

            case RUN_COMMAND:
                return EnumClickAction.RUN_COMMAND;

            case SUGGEST_COMMAND:
                return EnumClickAction.SUGGEST_COMMAND;

            case OPEN_URL:
                return EnumClickAction.OPEN_URL;

            case OPEN_FILE:
                return EnumClickAction.OPEN_FILE;

            default:
                return EnumClickAction.CHANGE_PAGE;
        }
    }

    @Nonnull
    private EnumHoverAction getAction(final HoverEvent.Action action) {

        switch (action) {

            case SHOW_TEXT:
                return EnumHoverAction.SHOW_TEXT;

            case SHOW_ITEM:
                return EnumHoverAction.SHOW_ITEM;

            case SHOW_ACHIEVEMENT:
//                return EnumHoverAction.SHOW_ACHIEVEMENT;

            case SHOW_ENTITY:
                return EnumHoverAction.SHOW_ENTITY;
        }

        return null;
    }
}