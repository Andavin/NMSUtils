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

package com.andavin.nbt;

import com.andavin.nbt.wrapper.NBTBase;
import com.andavin.nbt.wrapper.NBTHelper;
import com.andavin.nbt.wrapper.NBTTagCompound;
import com.andavin.nbt.wrapper.NBTType;
import com.andavin.reflect.Reflection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Andavin
 * @since May 15, 2018
 */
public final class ItemNBT {

    private static final Class<?> CRAFT_ITEM = Reflection.getCraftClass("inventory.CraftItemStack");
    private static final Field HANDLE = Reflection.getField(CRAFT_ITEM, "handle");
    private static final Field TAG, MAP = Reflection.getValue(NBTTagCompound.class, null, "DATA");

    private static final Method CRAFT_COPY = Reflection.getMethod(CRAFT_ITEM, "asCraftCopy", ItemStack.class);
    private static final Method NMS_COPY = Reflection.getMethod(CRAFT_ITEM, "asNMSCopy", ItemStack.class);
    private static final Method CRAFT_MIRROR;

    static {
        final Class<?> itemClass = Reflection.getMcClass("ItemStack");
        TAG = Reflection.getField(itemClass, "tag");
        CRAFT_MIRROR = Reflection.getMethod(CRAFT_ITEM, "asCraftMirror", itemClass);
    }

    /**
     * Check if the given {@link ItemStack item} is empty in that
     * if it is {@code null} or {@link Material#AIR air} it is empty.
     *
     * @param item The item in question.
     * @return If the item is empty.
     */
    public static boolean isEmpty(final ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Tell if the given item has an NBTTag with one of the given keys.
     *
     * @param item The item to check for NBT.
     * @param keys The key to check for.
     * @return If one of the given keys is present as an NBT tag.
     */
    public static boolean hasTag(final ItemStack item, final String... keys) {

        if (isEmpty(item) || keys.length == 0 || !CRAFT_ITEM.isInstance(item)) {
            return false;
        }

        final Object nms = getNmsItemStack(item);
        final Object nbt = Reflection.getValue(TAG, nms);
        if (nbt == null) {
            return false;
        }

        final Map<String, Object> map = Reflection.getValue(MAP, nbt);
        for (final String key : keys) {

            if (map.containsKey(key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the {@link NBTTagCompound tag} that is the main
     * NBT tag on the given {@link ItemStack}.
     * <p>
     * Note that if the given ItemStack is not an instance of
     * {@code CraftItemStack}, then {@code null} will be returned.
     * {@link #ensureCraftItem(ItemStack)} may be used to ensure
     * the instance of the ItemStack.
     *
     * @param item The item to get the NBT tag for.
     * @return The NBT tag on the item or {@code null} if there was none.
     * @see #ensureCraftItem(ItemStack) Ensure CraftItemStack instance
     */
    @Nullable
    public static NBTTagCompound getTag(final ItemStack item) {

        if (isEmpty(item) || !CRAFT_ITEM.isInstance(item)) {
            return null;
        }

        final Object nbt = Reflection.getValue(TAG, getNmsItemStack(item));
        return nbt != null ? NBTHelper.wrap(NBTType.COMPOUND, nbt) : null;
    }

    /**
     * Get the {@link NBTTagCompound tag} that is the main
     * NBT tag on the given {@link ItemStack} or create a new
     * NBT tag and add it to the item. The only time that this
     * will return {@code null} is if the passed ItemStack is
     * either {@code null} or {@link Material#AIR}.
     * <p>
     * This method requires that the passed in ItemStack be an
     * instance of {@code CraftItemStack}. If this is not so,
     * then this method will fail with an exception.
     * <p>
     * {@link #ensureCraftItem(ItemStack)} may be used to ensure
     * the instance of the ItemStack.
     *
     * @param item The item to get the NBT tag for.
     * @return The NBT tag on the item or create one and set it on the
     *         item if there was none.
     * @throws IllegalArgumentException If the item is not an {@code CraftItemStack}.
     * @see #ensureCraftItem(ItemStack) Ensure CraftItemStack instance helper
     */
    public static NBTTagCompound getOrCreateTag(final ItemStack item) throws IllegalArgumentException {

        checkArgument(CRAFT_ITEM.isInstance(item), "must be CraftItemStack");
        if (isEmpty(item)) {
            return null;
        }

        final Object nms = getNmsItemStack(item);
        final Object nbt = Reflection.getValue(TAG, nms);
        if (nbt == null) {
            final NBTTagCompound tag = new NBTTagCompound();
            Reflection.setValue(TAG, nms, tag.getWrapped());
            return tag;
        }

        return NBTHelper.wrap(NBTType.COMPOUND, nbt);
    }

    /**
     * Get the NBTBase tag on the given ItemStack with the given key.
     * If there is no tag or the parameters were null this method will
     * return {@code null}.
     *
     * @param item The item to get the tag from.
     * @param key The key of the NBT tag.
     * @param <T> The type of {@link NBTBase} tag to retrieve.
     * @return The NBTBase tag that was on the item.
     */
    public static <T extends NBTBase> T getTag(final ItemStack item, final String key) {

        if (isEmpty(item) || key == null || key.isEmpty() || !CRAFT_ITEM.isInstance(item)) {
            return null;
        }

        final Object nbt = Reflection.getValue(TAG, getNmsItemStack(item));
        if (nbt == null) {
            return null;
        }

        final Object tag = Reflection.<Map<String, Object>>getValue(MAP, nbt).get(key);
        return tag != null ? NBTHelper.wrap(tag) : null;
    }

    /**
     * Set the given NBT tag onto the given item under the given key.
     *
     * @param item The item to set the tag onto.
     * @param key The key to set the tag under.
     * @param tag The NBT tag to set under the key.
     * @return The item that has the tag on it (may or may not be the same item instance).
     */
    public static ItemStack setTag(final ItemStack item, final String key, final NBTBase tag) {

        checkNotNull(tag, "tag cannot be null");
        if (isEmpty(item) || key == null || key.isEmpty()) {
            return item;
        }

        // Get the NMS ItemStack ... If the ItemStack was already
        // an instance of CraftItemStack then the matching NMS ItemStack
        // will be returned else there will be a copy given
        final Object nms = getNmsItemStack(item);
        final Object nbt = Reflection.getValue(TAG, nms);
        if (nbt != null) {
            // If the tag already exists pull it. If it doesn't, create a new one.
            final Map<String, Object> map = Reflection.getValue(MAP, nbt);
            map.put(key, tag.getWrapped()); // Set our value under the key
        } else {
            // No need to pull current info because there isn't any.
            Reflection.setValue(TAG, nms, new NBTTagCompound(          // If they item didn't exist above,
                    Collections.singletonMap(key, tag)).getWrapped()); // then set the new tag onto the item
        }

        // If the item was instanceof CraftItemStack then we just edited
        // the basic object so we can just pass itself back.
        // Otherwise, we need to create a Bukkit mirror of the new NMS ItemStack.
        return CRAFT_ITEM.isInstance(item) ? item : Reflection.invokeMethod(CRAFT_MIRROR, null, nms);
    }

    /**
     * Set multiple NBT tags onto the given item under their relative keys.
     *
     * @param item The item to set the tags onto.
     * @param nbtTags The tags to set onto the item already mapped to their keys.
     * @return The item that has the tags on it (may or may not be the same item instance).
     */
    public static ItemStack setTags(final ItemStack item, final Map<String, NBTBase> nbtTags) {

        if (isEmpty(item) || nbtTags == null || nbtTags.isEmpty()) {
            return item;
        }

        final Object nms = getNmsItemStack(item);
        final Object nbt = Reflection.getValue(TAG, nms);
        if (nbt != null) {
            final Map<String, Object> map = Reflection.getValue(MAP, nbt);
            nbtTags.forEach((key, tag) -> map.put(key, tag.getWrapped()));
            return item;
        }

        Reflection.setValue(TAG, nms, new NBTTagCompound(nbtTags).getWrapped());
        return Reflection.invokeMethod(CRAFT_MIRROR, null, nms);
    }

    /**
     * Remove the NBT tag mapped under the given key from the given item.
     *
     * @param item The item to remove the tag from.
     * @param key The key the tag is mapped under.
     * @return If a tag existed under the given key and was
     *         successfully removed.
     */
    public static boolean removeTag(final ItemStack item, final String key) {

        if (!isEmpty(item) && key != null && !key.isEmpty() && CRAFT_ITEM.isInstance(item)) {

            final Object nbt = Reflection.getValue(TAG, getNmsItemStack(item));
            if (nbt != null) {
                return Reflection.<Map<String, Object>>getValue(MAP, nbt).remove(key) != null;
            }
        }

        return false;
    }

    /**
     * Ensure that the given {@link ItemStack} is an instance of
     * {@code CraftItemStack} and, if not, then return a new one.
     *
     * @param item The ItemStack to ensure.
     * @return The ItemStack that is an instance of {@code CraftItemStack}.
     */
    public static ItemStack ensureCraftItem(final ItemStack item) {
        return CRAFT_ITEM.isInstance(item) ? item : Reflection.invokeMethod(CRAFT_COPY, null, item);
    }

    /**
     * Get the {@code NMS ItemStack} equivalent of the Bukkit
     * version of the given {@link ItemStack item}.
     * <p>
     * Note that if the given Bukkit ItemStack was not an instance
     * of {@code CraftItemStack}, then the returned stack will not
     * be the one that is attached to the parameter and therefore
     * edits made to it may not be directly applicable to the parameter.
     *
     * @param item The item to get the NMS version for.
     * @return The NMS item stack.
     */
    public static Object getNmsItemStack(final ItemStack item) {
        return CRAFT_ITEM.isInstance(item) ? Reflection.getValue(HANDLE, item) :
                Reflection.invokeMethod(NMS_COPY, null, item);
    }
}
