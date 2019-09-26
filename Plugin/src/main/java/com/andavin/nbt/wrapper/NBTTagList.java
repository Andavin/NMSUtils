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

package com.andavin.nbt.wrapper;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.*;

import static com.andavin.reflect.Reflection.*;

/**
 * An NBT wrapper for the {@code NBTTagList} NMS class. This is
 * very similar to a {@link List} and is, in fact, backed by a {@code List}.
 * <p>
 * Unlike {@code NMS NBTTagList}, this implements {@link Iterable}
 * which allows for native foreach loops and iterator creators.
 *
 * @author Andavin
 * @since May 12, 2018
 */
@NBTTag(typeId = NBTType.LIST)
public final class NBTTagList<E extends NBTBase> extends NBTBase<List<E>> implements Iterable<E> {

    private static final Field DATA = findField(findMcClass("NBTTagList"), "list");

    private byte tagType = -1;
    private final List<E> wrapped;
    private transient List<Object> list;

    /**
     * Create a new, empty list tag.
     */
    public NBTTagList() {
        this(NBTHelper.createTag(NBTTagList.class));
    }

    /**
     * Create a new list tag from a {@link List} that is
     * the type of this list and the types are all the same
     * {@link NBTBase#getTypeId() type ID}.
     *
     * @param list The List to create the tag from.
     */
    public NBTTagList(List<E> list) {
        this();
        list.forEach(this::add);
    }

    NBTTagList(Object wrapped) {
        super(wrapped);
        this.list = getFieldValue(DATA, wrapped);
        if (!this.list.isEmpty()) {
            this.wrapped = new ArrayList<>(this.list.size());
            this.list.forEach(nbt -> this.wrapped.add(NBTHelper.wrap(nbt)));
        } else {
            this.wrapped = new ArrayList<>();
        }
    }

    /**
     * Get how many elements that are currently
     * stored in this list.
     * <p>
     * This is equivalent to {@link List#size()}.
     *
     * @return The amount of elements in this list.
     */
    public int size() {
        return this.list.size();
    }

    /**
     * The type of tag that will be stored in this
     * list. If this is {@code -1} then the type has
     * not been set yet and will be the first element
     * that is added to the list.
     *
     * @return The type of tag that this list stores.
     */
    public byte getTagType() {
        return tagType;
    }

    @Override
    public List<E> getData() {
        return Collections.unmodifiableList(this.wrapped);
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    /**
     * Add a new {@link NBTBase tag} to this list. The tag cannot
     * be an {@link NBTTagEnd} and must match {@link NBTBase#getTypeId()
     * tag type} with {@link #getTagType() this tag type}.
     * <p>
     * If tag types do not match, then this method will do nothing.
     *
     * @param tag The tag to add to this list.
     * @return If the tag was successfully added.
     */
    public boolean add(E tag) {

        if (this.isType(tag)) {
            this.wrapped.add(tag);
            return this.list.add(tag.wrapped);
        }

        return false;
    }

    /**
     * Add a new {@link NBTBase tag} to this list at the given index
     * and shift all of the other tags to the right. The tag cannot
     * be an {@link NBTTagEnd} and must match {@link NBTBase#getTypeId()
     * tag type} with {@link #getTagType() this tag type}.
     * <p>
     * If the index is not in range of the list or if the types do not
     * match, then this method will do nothing.
     *
     * @param index The index to add the tag at.
     * @param tag The tag to add to this list.
     */
    public void add(int index, E tag) {

        if (this.isInRange(index) && this.isType(tag)) {
            this.wrapped.add(index, tag);
            this.list.add(index, tag.wrapped);
        }
    }

    /**
     * Replace the tag at the given index with the given new tag.
     * The tag cannot be an {@link NBTTagEnd} and must match
     * {@link NBTBase#getTypeId() tag type} with {@link #getTagType()
     * this tag type}.
     * <p>
     * If the index is not in range of the list or if the types do not
     * match, then this method will do nothing.
     *
     * @param index The index to replace at.
     * @param tag The tag to replace the index with.
     * @return The {@link NBTBase tag} that was previously at the index.
     */
    public NBTBase set(int index, E tag) {

        if (this.isInRange(index) && this.isType(tag)) {
            this.list.set(index, tag);
            return this.wrapped.set(index, tag);
        }

        return null;
    }

    /**
     * Get the {@link NBTBase tag} at the given index.
     * <p>
     * This is equivalent to {@link List#get(int)}.
     *
     * @param index The index to get the tag at.
     * @return The tag at the given index.
     */
    public E get(int index) {
        return this.wrapped.get(index);
    }

    /**
     * Removes the first occurrence of an {@link NBTBase NBT tag}
     * equivalent to the given tag.
     *
     * @param tag The tag to remove the equivalents.
     * @return If any tag was successfully removed.
     */
    public boolean remove(E tag) {
        this.list.remove(tag.wrapped);
        return this.wrapped.remove(tag);
    }

    /**
     * Remove the {@link NBTBase tag} located at the given index.
     * <p>
     * This is equivalent to {@link List#remove(int)}.
     *
     * @param index The index to remove the tag at.
     * @return The tag that was removed.
     */
    public NBTBase remove(int index) {
        this.list.remove(index);
        return this.wrapped.remove(index);
    }

    @Override
    public Iterator<E> iterator() {
        return new DualIteratorDelegate<>(this.wrapped.iterator(), this.list.iterator());
    }

    private boolean isInRange(int index) {
        return 0 <= index && index < this.list.size();
    }

    private boolean isType(NBTBase tag) {

        byte id = tag.getTypeId();
        if (id == NBTType.END) {
            return false;
        }

        if (this.tagType == -1) {
            this.tagType = id;
            return true;
        }

        return this.tagType == id;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.list = getFieldValue(DATA, this.getWrapped());
        if (!this.wrapped.isEmpty()) {
            this.wrapped.stream().map(NBTBase::getWrapped).forEach(this.list::add);
        }
    }

    /**
     * Deserialize the map into a new NBTBase object as
     * specified by {@link ConfigurationSerializable}.
     *
     * @param map The map that was serialized with
     *        {@link ConfigurationSerializable#serialize()}.
     * @return The newly created, deserialized object.
     */
    public static NBTTagList deserialize(Map<String, Object> map) {
        return new NBTTagList((List<NBTBase>) map.get("data"));
    }
}
