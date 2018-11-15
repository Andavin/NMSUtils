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

package com.andavin.inventory;

import com.andavin.Versioned;
import com.andavin.nbt.ItemNBT;
import com.andavin.nbt.wrapper.NBTHelper;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A basic helper class to do common tasks that have to do
 * with {@link ItemStack} such as checking if the
 * {@link #isEmpty(ItemStack) item is nothing} or
 * {@link #serialize(ItemStack) serializing} the item.
 *
 * @since November 07, 2018
 * @author Andavin
 * @see ItemNBT
 */
public final class ItemHelper {

    private static final ItemBridge INSTANCE = Versioned.getInstance(ItemBridge.class);

    /**
     * Check if the given {@link ItemStack item} is empty in that
     * if it is {@code null} or {@link Material#AIR air} it is empty.
     *
     * @param item The item in question.
     * @return If the item is empty.
     */
    public static boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    /**
     * Tell whether the given {@link ItemStack} is an instance
     * of {@code CraftItemStack} (the implementation of ItemStack
     * contained in the {@code inventory} package of CraftBukkit).
     *
     * @param item The ItemStack to test.
     * @return If the ItemStack is an instance of {@code CraftItemStack.}
     */
    public static boolean isCraftItem(ItemStack item) {
        return INSTANCE.isCraftItem(item);
    }

    /**
     * Ensure that the given {@link ItemStack} is an instance of
     * {@code CraftItemStack} and, if not, then return a new one.
     *
     * @param item The ItemStack to ensure.
     * @return The ItemStack that is an instance of {@code CraftItemStack}.
     */
    public static ItemStack ensureCraftItem(ItemStack item) {
        return INSTANCE.ensureCraftItem(item);
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
    public static Object getNmsItemStack(ItemStack item) {
        return INSTANCE.getNmsItemStack(item);
    }

    /**
     * Damage the given item (do durability calculation) just as it would
     * be with a vanilla Minecraft item.
     * <p>
     * Note that calling this method will call events to do with damaging
     * items such as {@link PlayerItemDamageEvent} and {@link PlayerItemBreakEvent}.
     *
     * @param item The item to damage.
     * @param amount The amount of damage to do to the item.
     * @param livingEntity The {@link LivingEntity} that is using the item
     *                     and causing it to be damaged.
     */
    public static void damageItem(ItemStack item, int amount, LivingEntity livingEntity) {
        INSTANCE.damageItem(item, amount, livingEntity);
    }

    /**
     * Serialize the given {@link ItemStack} into a byte array
     * that can be used to fully reconstruct the ItemStack later on.
     *
     * @param item The item to serialize.
     * @return The byte array for the serialized item.
     * @throws UncheckedIOException If something goes wrong during serialization.
     * @see #deserialize(byte[])
     */
    public static byte[] serialize(ItemStack item) throws UncheckedIOException {

        Object tag = INSTANCE.saveToNBT(item);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            NBTHelper.serialize(stream, tag);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Deserialize the given byte array back into an {@link ItemStack}.
     * Ideally, the array should come from the {@link #serialize(ItemStack)}
     * method, but any form of NBT serialized item should technically work here.
     *
     * @param bytes The bytes to deserialize.
     * @return The newly created ItemStack for the bytes.
     * @throws UncheckedIOException If something goes wrong during deserialization.
     * @see #serialize(ItemStack)
     */
    public static ItemStack deserialize(byte[] bytes) throws UncheckedIOException {

        try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
            return INSTANCE.createStack(NBTHelper.deserializeNMS(stream));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
