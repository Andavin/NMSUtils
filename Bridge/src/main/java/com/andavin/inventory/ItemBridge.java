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
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * @since November 13, 2018
 * @author Andavin
 */
public abstract class ItemBridge implements Versioned {

    protected ItemBridge() {
    }

    /**
     * Tell whether the given {@link ItemStack} is an instance
     * of {@code CraftItemStack} (the implementation of ItemStack
     * contained in the {@code inventory} package of CraftBukkit).
     *
     * @param item The ItemStack to test.
     * @return If the ItemStack is an instance of {@code CraftItemStack.}
     */
    protected abstract boolean isCraftItem(ItemStack item);

    /**
     * Ensure that the given {@link ItemStack} is an instance of
     * {@code CraftItemStack} and, if not, then return a new one.
     *
     * @param item The ItemStack to ensure.
     * @return The ItemStack that is an instance of {@code CraftItemStack}.
     */
    protected abstract ItemStack ensureCraftItem(ItemStack item);

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
    protected abstract <T> T getNmsItemStack(ItemStack item);

    /**
     * Create a new {@link ItemStack} from an {@code NBTTagCompound}.
     *
     * @param tagObj The {@code NBTTagCompound} to create the item from.
     * @return The newly created ItemStack.
     */
    protected abstract ItemStack createStack(Object tagObj);

    /**
     * Save an {@link ItemStack} to an {@code NBTTagCompound} object.
     *
     * @param item The item to save.
     * @return The compound object the item was saved to.
     */
    protected abstract Object saveToNBT(ItemStack item);

    /**
     * Damage the given item (do durability calculation) just as it would
     * be with a vanilla Minecraft item.
     *
     * @param item The item to damage.
     * @param amount The amount of damage to do to the item.
     * @param livingEntity The {@link LivingEntity} that is using the item
     *                     and causing it to be damaged.
     */
    protected abstract void damageItem(ItemStack item, int amount, LivingEntity livingEntity);
}
