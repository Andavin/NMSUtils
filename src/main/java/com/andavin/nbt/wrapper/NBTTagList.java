package com.andavin.nbt.wrapper;

import com.andavin.DataHolder;
import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public final class NBTTagList extends NBTBase implements DataHolder<List<NBTBase>>, Iterable<NBTBase> {

    private static final Field DATA = Reflection.getField(Reflection.getMcClass("NBTTagList"), "list");

    private byte tagType = -1;
    private final List<Object> list;
    private final List<NBTBase> wrapped;

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
    public NBTTagList(final List<NBTBase> list) {
        this();
        list.forEach(this::add);
    }

    NBTTagList(final Object wrapped) {
        super(wrapped);
        this.list = Reflection.getValue(DATA, wrapped);
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
    public List<NBTBase> getData() {
        return this.wrapped;
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
    public boolean add(final NBTBase tag) {

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
    public void add(final int index, final NBTBase tag) {

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
    public NBTBase set(final int index, final NBTBase tag) {

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
    public NBTBase get(final int index) {
        return this.wrapped.get(index);
    }

    /**
     * Removes the first occurrence of an {@link NBTBase NBT tag}
     * equivalent to the given tag.
     *
     * @param tag The tag to remove the equivalents.
     * @return If any tag was successfully removed.
     */
    public boolean remove(final NBTBase tag) {
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
    public NBTBase remove(final int index) {
        this.list.remove(index);
        return this.wrapped.remove(index);
    }

    /**
     * Deserialize the map into a new NBTBase object as
     * specified by {@link ConfigurationSerializable}.
     *
     * @param map The map that was serialized with
     *        {@link ConfigurationSerializable#serialize()}.
     * @return The newly created, deserialized object.
     */
    public static NBTTagList deserialize(final Map<String, Object> map) {
        //noinspection unchecked
        return new NBTTagList((List<NBTBase>) map.get("data"));
    }

    @Override
    public Iterator<NBTBase> iterator() {
        return this.wrapped.iterator();
    }

    private boolean isInRange(final int index) {
        return 0 <= index && index < this.list.size();
    }

    private boolean isType(final NBTBase tag) {

        final byte id = tag.getTypeId();
        if (id == NBTType.END) {
            return false;
        }

        if (this.tagType == -1) {
            this.tagType = id;
            return true;
        } else {
            return this.tagType == id;
        }
    }
}