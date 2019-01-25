///*
// * MIT License
// *
// * Copyright (c) 2018 Andavin
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package com.andavin.chat;
//
//import net.md_5.bungee.api.chat.ClickEvent;
//import net.md_5.bungee.api.chat.HoverEvent;
//import org.bukkit.ChatColor;
//
//import java.util.List;
//
//import static com.google.common.base.Preconditions.checkArgument;
//
///**
// * A chat utility used for editing specific words in a sentence.
// * Editing these words is as simple as learning how to edit a
// * single ChatComponent as it works exactly the same way. Any
// * methods used in this class will edit all ChatComponents
// * contained within.
// * <p>
// * A highlighter is obtained by using the {@link ChatComponent#highlight(String, boolean)}
// * method in the ChatComponent class.
// *
// * @see ChatComponent
// * @see ChatComponent#highlight(String, boolean)
// * @see ChatComponent#highlight(boolean, String...)
// */
//public final class Highlighter {
//
//    private final List<ChatComponent> comps;
//
//    Highlighter(List<ChatComponent> comps) {
//        this.comps = comps;
//    }
//
//    /**
//     * Set the color of all ChatComponents in this Highlighter.
//     *
//     * @param color The color to set to.
//     * @return A reference to this Highlighter.
//     * @throws IllegalArgumentException If the ChatColor is not a color.
//     */
//    public Highlighter color(ChatColor color) {
//        checkArgument(color.isColor(), "Color must be a color, but got %s instead.", color.name());
//        this.comps.forEach(comp -> comp.color(color));
//        return this;
//    }
//
//    /**
//     * Toggle the bold style for all of the
//     * ChatComponents held inside this Highlighter.
//     *
//     * @param set Whether it should be set on or off.
//     * @return A reference to this Highlighter.
//     */
//    public Highlighter bold(boolean set) {
//        this.comps.forEach(comp -> comp.bold(set));
//        return this;
//    }
//
//    /**
//     * Toggle the italic style for all of the
//     * ChatComponents held inside this Highlighter.
//     *
//     * @param set Whether it should be set on or off.
//     * @return A reference to this Highlighter.
//     */
//    public Highlighter italic(boolean set) {
//        this.comps.forEach(comp -> comp.italic(set));
//        return this;
//    }
//
//    /**
//     * Toggle the magic style for all of the
//     * ChatComponents held inside this Highlighter.
//     *
//     * @param set Whether it should be set on or off.
//     * @return A reference to this Highlighter.
//     */
//    public Highlighter magic(boolean set) {
//        this.comps.forEach(comp -> comp.magic(set));
//        return this;
//    }
//
//    /**
//     * Toggle the strikethrough style for all of the
//     * ChatComponents held inside this Highlighter.
//     *
//     * @param set Whether it should be set on or off.
//     * @return A reference to this Highlighter.
//     */
//    public Highlighter strikethrough(boolean set) {
//        this.comps.forEach(comp -> comp.strikethrough(set));
//        return this;
//    }
//
//    /**
//     * Toggle the underlined style for all of the
//     * ChatComponents held inside this Highlighter.
//     *
//     * @param set Whether it should be set on or off.
//     * @return A reference to this Highlighter.
//     */
//    public Highlighter underline(boolean set) {
//        this.comps.forEach(comp -> comp.underlined(set));
//        return this;
//    }
//
//    /**
//     * Set all the ChatComponents in this Highlighter to perform
//     * an action when clicked.
//     * <p>
//     * <b>Important:</b> You must avoid using any unicode or
//     * symbols that are not allowed in Minecraft chat in this method
//     * click string (such as the section symbol §). If used Minecraft
//     * will throw an exception on use of the click action.
//     *
//     * @param action The action to perform.
//     * @param click The command to run, file to be opened, etc.
//     * @return A reference to this object.
//     */
//    public Highlighter event(ClickEvent.Action action, String click) {
//        this.comps.forEach(comp -> comp.event(action, click));
//        return this;
//    }
//
//    /**
//     * Set all the ChatComponents in this Highlighter to show
//     * some text, an image, an entity, etc. whenever it is hovered
//     * over. This is also known as a tooltip.
//     *
//     * @param action What kind thing to show when hovered over.
//     * @param hover What to show when hovered over.
//     * @return A reference to this object.
//     */
//    public Highlighter event(HoverEvent.Action action, ChatComponent hover) {
//        this.comps.forEach(comp -> comp.event(action, hover));
//        return this;
//    }
//
//    /**
//     * Get the length of how many ChatComponents
//     * are held inside of this Highlighter.
//     *
//     * @return The number of ChatComponents.
//     */
//    public int length() {
//        return this.comps.size();
//    }
//
//    /**
//     * Get the ChatComponent list that this Highlighter
//     * is editing.
//     *
//     * @return The ChatComponents being edited.
//     */
//    public List<ChatComponent> getComps() {
//        return this.comps;
//    }
//}
