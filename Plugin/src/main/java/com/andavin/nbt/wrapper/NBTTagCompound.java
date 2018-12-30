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

import com.andavin.DataHolder;
import com.andavin.reflect.Reflection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static com.andavin.reflect.Reflection.*;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An NBT wrapper for the {@code NBTTagCompound} NMS class.
 * This is very similar to a {@link Map} and is, in fact, backed by
 * a {@code Map}.
 * <p>
 * There are a couple extra methods provided in this class over the NMS
 * wrapped class. These include
 * <ul>
 *     <li>{@link #forEach(BiConsumer)} for iterations.</li>
 *     <li>{@link #get(String)} with the generic return type.</li>
 * </ul>
 * There are also performance improvements in the map interactions
 * while having a better type conformation with the retrieval methods.
 * <p>
 * Note that this class is <i>not</i> built for concurrency of any
 * kind and is even more susceptible to concurrent modification issues
 * than a regular {@link HashMap}.
 *
 * @author Andavin
 * @since May 12, 2018
 * @see NBTHelper
 * @see NBTHelper#serialize(OutputStream, NBTTagCompound)
 * @see NBTHelper#deserialize(InputStream)
 */
@NBTTag(typeId = NBTType.COMPOUND)
public final class NBTTagCompound extends NBTBase implements DataHolder<Map<String, NBTBase>> {

    private static final Field DATA = findField(findMcClass("NBTTagCompound"), "map");
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];

    private transient Set<String> keySet;
    private transient Collection<NBTBase> values;
    private transient Set<Entry<String, NBTBase>> entrySet;

    private final Map<String, NBTBase> wrapped;
    private transient Map<String, Object> map;

    /**
     * Create a new, empty compound tag.
     */
    public NBTTagCompound() {
        this(NBTHelper.createTag(NBTTagCompound.class));
    }

    /**
     * Create a new compound tag from a {@link Map} that is
     * keyed by string to {@link NBTBase} values.
     *
     * @param map The Map to create the compound from.
     */
    public NBTTagCompound(Map<String, NBTBase> map) {
        this();
        map.forEach(this::set);
    }

    NBTTagCompound(Object wrapped) {
        super(wrapped);
        this.map = Reflection.getFieldValue(DATA, wrapped);
        if (!this.map.isEmpty()) {
            this.wrapped = new HashMap<>(this.map.size());
            this.map.forEach((key, value) -> this.wrapped.put(key, NBTHelper.wrap(value)));
        } else {
            this.wrapped = new HashMap<>();
        }
    }

    /**
     * Get the current {@link Set} of keys that are mapped to
     * values in this compound tag.
     * <p>
     * This is equivalent to {@link Map#keySet()}.
     *
     * @return The set of keys for this tag.
     */
    public Set<String> keySet() {
        return this.keySet != null ? this.keySet :
                (this.keySet = new DualSetDelegate(this.wrapped.keySet(), this.map.keySet()));
    }

    /**
     * Get the current {@link Collection} of values that are
     * mapped to keys in this compound tag.
     * <p>
     * This is equivalent to {@link Map#values()}.
     *
     * @return The tag value collection for this tag.
     */
    public Collection<NBTBase> values() {
        return this.values != null ? this.values :
                (this.values = new DualCollectionDelegate(this.wrapped.values(), this.map.values()));
    }

    /**
     * Get the current {@link Set} of the entries in this
     * compound tag. Each entry will contain its key and
     * the tag value that it is mapped to.
     * <p>
     * This is equivalent to {@link Map#entrySet()}.
     *
     * @return The set of entries for this tag.
     */
    public Set<Entry<String, NBTBase>> entrySet() {
        return this.entrySet != null ? this.entrySet :
                (this.entrySet = new DualSetDelegate(this.wrapped.entrySet(), this.map.entrySet()));
    }

    /**
     * Get the number of entries currently present in this
     * compound tag.
     * <p>
     * This is equivalent to {@link Map#size()}.
     *
     * @return The current map size.
     */
    public int size() {
        return this.map.size();
    }

    @Override
    public Map<String, NBTBase> getData() {
        return Collections.unmodifiableMap(this.wrapped);
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * Clear all of the entries from this compound tag
     * so that it contains no tags.
     * <p>
     * This is equivalent to {@link Map#clear()}.
     */
    public void clear() {
        this.map.clear();
        this.wrapped.clear();
    }

    /**
     * Set an already created {@link NBTBase} into the storage
     * in this compound tag; mapping it under the give key.
     * <p>
     * This is very similar to a {@link Map#put(Object, Object)}.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@link NBTBase value} to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase set(String key, NBTBase value) {
        checkNotNull(key, "null keys not permitted");
        checkNotNull(value, "null values not permitted");
        this.map.put(key, value.wrapped); // Will be the wrapped object of old
        return this.wrapped.put(key, value);
    }

    /**
     * Wrap the given primitive {@code byte} into an {@link NBTTagByte}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code byte} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setByte(String key, byte value) {
        return this.set(key, new NBTTagByte(value));
    }

    /**
     * Wrap the given primitive {@code short} into an {@link NBTTagShort}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code short} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setShort(String key, short value) {
        return this.set(key, new NBTTagShort(value));
    }

    /**
     * Wrap the given primitive {@code int} into an {@link NBTTagInt}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code int} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setInt(String key, int value) {
        return this.set(key, new NBTTagInt(value));
    }

    /**
     * Wrap the given primitive {@code long} into an {@link NBTTagLong}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code long} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setLong(String key, long value) {
        return this.set(key, new NBTTagLong(value));
    }

    /**
     * Wrap the given primitive {@code float} into an {@link NBTTagFloat}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code float} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setFloat(String key, float value) {
        return this.set(key, new NBTTagFloat(value));
    }

    /**
     * Wrap the given primitive {@code double} into an {@link NBTTagDouble}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code double} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setDouble(String key, double value) {
        return this.set(key, new NBTTagDouble(value));
    }

    /**
     * Wrap the given primitive {@code boolean} into an {@link NBTTagByte}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code boolean} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setBoolean(String key, boolean value) {
        return this.setByte(key, (byte) (value ? 1 : 0));
    }

    /**
     * Wrap the given {@link String} into an {@link NBTTagString}
     * and set it using the {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code String} value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setString(String key, String value) {
        return this.set(key, new NBTTagString(value));
    }

    /**
     * Wrap the given primitive {@code byte[]} array into an
     * {@link NBTTagByteArray} and set it using the
     * {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code byte[]} array value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setByteArray(String key, byte[] value) {
        return this.set(key, new NBTTagByteArray(value));
    }

    /**
     * Wrap the given primitive {@code int[]} array into an
     * {@link NBTTagIntArray} and set it using the
     * {@link #set(String, NBTBase)} method.
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code int[]} array value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setIntArray(String key, int[] value) {
        return this.set(key, new NBTTagIntArray(value));
    }

    /**
     * Wrap the given primitive {@code long[]} array into an
     * {@link NBTTagLongArray} and set it using the
     * {@link #set(String, NBTBase)} method.
     * <p>
     * <b>Warning</b> {@link NBTTagLongArray} is only supported
     * on server versions {@code 1.12} and above!
     *
     * @param key The {@link String key} to set the value under.
     * @param value The {@code long[]} array value to store under the key.
     * @return The previous value mapped to the key if there was one;
     *         {@code null} otherwise.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public NBTBase setLongArray(String key, int[] value) {
        return this.set(key, new NBTTagLongArray(value));
    }

    /**
     * Store a {@link UUID} by storing each of its {@code long} bits
     * under the {@code key + "Most"} and {@code key + "Least"}. Care
     * should be taken, therefore, in using keys that end in {@code Most}
     * or {@code Least} if the prefix is also going to be used for a
     * different type as it could collide keys with any {@code UUID} that
     * is also stored.
     *
     * @param key The {@link String} to set the value under.
     * @param uuid The {@link UUID value} to store under the key.
     * @throws NullPointerException If key or value are {@code null}.
     */
    public void setUniqueId(String key, UUID uuid) {
        checkNotNull(key, "null keys not permitted");
        this.setLong(key + "Most", uuid.getMostSignificantBits());
        this.setLong(key + "Least", uuid.getLeastSignificantBits());
    }

    /**
     * Get the {@link NBTBase} value stored under the given key.
     * If a the instance check to {@link T} needs to be omitted,
     * then the type {@linkplain NBTBase} should be used for {@link T}.
     * <p>
     * This is very similar to a {@link Map#get(Object)}.
     *
     * @param key The {@link String} to retrieve the mapped value from.
     * @param <T> The {@linkplain NBTBase type} of key expected to be stored
     *            under the key.
     * @return The {@link NBTBase value} stored under the key or {@code null}
     *         if either the key does not exist or the value type does is not
     *         an instance of {@link T}.
     */
    @Nullable
    public <T extends NBTBase> T get(String key) {

        try {
            return (T) this.wrapped.get(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Get the primitive {@code byte} mapped to the given key.
     * If the value is present and is any instance of {@link NBTNumber},
     * then the conformed {@code byte} will be retrieved from that type.
     * {@code 0} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code byte} value stored or {@code 0} if there was 
     *         no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public byte getByte(String key) {
        NBTNumber tag = this.get(key);
        return tag != null ? tag.asByte() : 0;
    }

    /**
     * Get the primitive {@code short} mapped to the given key.
     * If the value is present and is any instance of {@link NBTNumber},
     * then the conformed {@code short} will be retrieved from that type.
     * {@code 0} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code short} value stored or {@code 0} if there was 
     *         no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public short getShort(String key) {
        NBTNumber tag = this.get(key);
        return tag != null ? tag.asShort() : 0;
    }

    /**
     * Get the primitive {@code int} mapped to the given key.
     * If the value is present and is any instance of {@link NBTNumber},
     * then the conformed {@code int} will be retrieved from that type.
     * {@code 0} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code int} value stored or {@code 0} if there was
     *         no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public int getInteger(String key) {
        NBTNumber tag = this.get(key);
        return tag != null ? tag.asInt() : 0;
    }

    /**
     * Get the primitive {@code long} mapped to the given key.
     * If the value is present and is any instance of {@link NBTNumber},
     * then the conformed {@code long} will be retrieved from that type.
     * {@code 0} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code long} value stored or {@code 0} if there was
     *         no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public long getLong(String key) {
        NBTNumber tag = this.get(key);
        return tag != null ? tag.asLong() : 0;
    }

    /**
     * Get the primitive {@code float} mapped to the given key.
     * If the value is present and is any instance of {@link NBTNumber},
     * then the conformed {@code float} will be retrieved from that type.
     * {@code 0} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code float} value stored or {@code 0} if there was
     *         no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public float getFloat(String key) {
        NBTNumber tag = this.get(key);
        return tag != null ? tag.asFloat() : 0;
    }

    /**
     * Get the primitive {@code double} mapped to the given key.
     * If the value is present and is any instance of {@link NBTNumber},
     * then the conformed {@code double} will be retrieved from that type.
     * {@code 0} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code double} value stored or {@code 0} if there was
     *         no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public double getDouble(String key) {
        NBTNumber tag = this.get(key);
        return tag != null ? tag.asDouble() : 0;
    }

    /**
     * Get the primitive {@code boolean} mapped to the given key.
     * If the value is present and is any instance of {@linkplain NBTNumber},
     * then the conformed {@link boolean} will be retrieved from that type.
     * {@code false} will be returned otherwise.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@code boolean} value stored or {@code false} if there
     *         was no mapping or if a non-{@linkplain NBTNumber} was stored there.
     */
    public boolean getBoolean(String key) {
        return this.getByte(key) != 0;
    }

    /**
     * Get the {@link String} mapped to the given key.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The value stored at the location or an empty String
     *         ({@code ""}) if there was no mapping for the key.
     */
    @Nonnull
    public String getString(String key) {
        NBTTagString tag = this.get(key);
        return tag != null ? tag.toString() : "";
    }

    /**
     * Get the {@code byte[]} mapped to the given key.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The value stored at the location or an empty array
     *         ({@code byte[0]}) if there was no mapping for the key
     *         or if the mapping was not of the type {@link NBTTagLongArray}.
     */
    @Nonnull
    public byte[] getByteArray(String key) {
        NBTTagByteArray tag = this.get(key);
        return tag != null ? tag.getData() : EMPTY_BYTE_ARRAY;
    }

    /**
     * Get the {@code int[]} mapped to the given key.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The value stored at the location or an empty array
     *         ({@code int[0]}) if there was no mapping for the key
     *         or if the mapping was not of the type {@link NBTTagIntArray}.
     */
    @Nonnull
    public int[] getIntArray(String key) {
        NBTTagIntArray tag = this.get(key);
        return tag != null ? tag.getData() : EMPTY_INT_ARRAY;
    }

    /**
     * Get the {@code long[]} mapped to the given key.
     * <p>
     * <b>Warning</b> {@link NBTTagLongArray} is only supported
     * on server versions {@code 1.12} and above!
     *
     * @param key The key to retrieve the value mapping from.
     * @return The value stored at the location or an empty array
     *         ({@code long[0]}) if there was no mapping for the key
     *         or if the mapping was not of the type {@link NBTTagLongArray}.
     */
    @Nonnull
    public long[] getLongArray(String key) {
        NBTTagLongArray tag = this.get(key);
        return tag != null ? tag.getData() : EMPTY_LONG_ARRAY;
    }

    /**
     * Get the {@link UUID} mapped to the given key.
     *
     * @param key The key to retrieve the value mapping from.
     * @return The {@linkplain UUID value} stored at the location
     *         or null if there was no mapping for the key or if
     *         the mappings did not conform to {@code long}s properly.
     */
    @Nullable
    public UUID getUniqueId(String key) {
        NBTTagLong most = this.get(key + "Most"), least = this.get(key + "Least");
        return most != null && least != null ? new UUID(most.asLong(), least.asLong()) : null;
    }

    /**
     * Get the integer ID type of the value mapped to the given key.
     *
     * @param key The key to retrieve the value type ID from.
     * @return The ID of the value mapped to the given key or
     *         {@link NBTType#END} if there was no mapping.
     */
    public byte getTypeAt(String key) {
        NBTBase value = this.get(key);
        return value != null ? value.getTypeId() : NBTType.END;
    }

    /**
     * Tell if there is currently a mapping for the given.
     * <p>
     * This is equivalent to {@link Map#containsKey(Object)}.
     *
     * @param key The key to check for a mapping at.
     * @return If there is currently a value stored at the given key.
     * @see NBTType NBT Type IDs
     */
    public boolean hasKey(String key) {
        return this.map.containsKey(key);
    }

    /**
     * Tell if this compound tag as a value of the given type
     * mapped to the given key.
     *
     * @param key The key to test the mapped value of.
     * @param typeId The ID to test the value for.
     * @return If this compound contains the key and the value
     *         has the matching {@link NBTBase#getTypeId() type ID}.
     * @see NBTType
     */
    public boolean hasKey(String key, int typeId) {

        byte type = this.getTypeAt(key);
        if (type == typeId) {
            return true;
        }

        if (typeId != NBTType.UUID) {
            return false;
        }

        switch (type) {
            case NBTType.BYTE:
            case NBTType.SHORT:
            case NBTType.INT:
            case NBTType.LONG:
            case NBTType.FLOAT:
            case NBTType.DOUBLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Tell if there is a {@link UUID} mapped to the given key.
     *
     * @param key The key to test for the {@linkplain UUID} mapping.
     * @return If there is a {@code UUID} mapped to the given key.
     */
    public boolean hasUniqueId(String key) {
        return this.hasKey(key + "Most", NBTType.UUID) && this.hasKey(key + "Least", NBTType.UUID);
    }

    /**
     * Remove the give key and any value that is mapped to
     * it if it is currently in this compound tag.
     * <p>
     * This is very similar to a {@link Map#remove(Object)}.
     *
     * @param key The key to remove.
     * @return The value mapped previously mapped to the key
     *         or {@code null} if no mapping existed.
     */
    public NBTBase remove(String key) {
        this.map.remove(key);
        return this.wrapped.remove(key);
    }

    /**
     * Iterate through this compound tag using the {@link BiConsumer}
     * to accept all of the keys and values.
     * <p>
     * This is equivalent to {@link Map#forEach(BiConsumer)}.
     *
     * @param action The action to take for each key-value pair.
     */
    public void forEach(BiConsumer<? super String, ? super NBTBase> action) {
        this.wrapped.forEach(action);
    }

    /**
     * Deserialize the map into a new NBTBase object as
     * specified by {@link ConfigurationSerializable}.
     *
     * @param map The map that was serialized with
     *        {@link ConfigurationSerializable#serialize()}.
     * @return The newly created, deserialized object.
     */
    public static NBTTagCompound deserialize(Map<String, Object> map) {
        return new NBTTagCompound((Map<String, NBTBase>) map.get("data"));
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.map = getFieldValue(DATA, this.wrapped);
        if (!this.wrapped.isEmpty()) {
            this.wrapped.forEach((key, value) -> this.map.put(key, value.wrapped));
        }
    }

    private class DualSetDelegate<T> extends AbstractSet<T> {

        private final Set<T> set, alt;

        DualSetDelegate(Set<T> set, Set<T> alt) {
            this.set = set;
            this.alt = alt;
        }

        @Override
        public void clear() {
            this.alt.clear();
            this.set.clear();
        }

        @Override
        public boolean contains(Object o) {
            return this.set.contains(o);
        }

        @Override
        @Nonnull
        public Iterator<T> iterator() {
            return new DualIteratorDelegate<>(this.set.iterator(), this.alt.iterator());
        }

        @Override
        public Spliterator<T> spliterator() {
            return this.set.spliterator();
        }

        @Override
        public int size() {
            return this.set.size();
        }
    }

    private class DualCollectionDelegate extends AbstractCollection<NBTBase> {

        private final Collection<?> alt;
        private final Collection<NBTBase> collection;

        DualCollectionDelegate(Collection<NBTBase> collection, Collection<?> alt) {
            this.collection = collection;
            this.alt = alt;
        }

        @Override
        public void clear() {
            this.alt.clear();
            this.collection.clear();
        }

        @Override
        public boolean contains(Object o) {
            return this.collection.contains(o);
        }

        @Override
        @Nonnull
        public Iterator<NBTBase> iterator() {
            return new DualIteratorDelegate<>(this.collection.iterator(), this.alt.iterator());
        }

        @Override
        public Spliterator<NBTBase> spliterator() {
            return this.collection.spliterator();
        }

        @Override
        public int size() {
            return this.collection.size();
        }
    }

}
