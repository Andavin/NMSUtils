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

package com.andavin.v1_13_R2.inventory;

import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;

import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.getFieldValue;

/**
 * @since November 13, 2018
 * @author Andavin
 */
class ItemBridge extends com.andavin.inventory.ItemBridge {

    private static final Field HANDLE = findField(CraftItemStack.class, "handle");

    @Override
    protected boolean isCraftItem(ItemStack item) {
        return item != null && CraftItemStack.class == item.getClass();
    }

    @Override
    protected ItemStack ensureCraftItem(ItemStack item) {
        return CraftItemStack.class == item.getClass() ? item : CraftItemStack.asCraftCopy(item);
    }

    @Override
    protected <T> T getNmsItemStack(ItemStack item) {
        return CraftItemStack.class == item.getClass() ? getFieldValue(HANDLE, item) :
                (T) CraftItemStack.asNMSCopy(item);
    }

    @Override
    public ItemStack createStack(Object tagObj) {
        return CraftItemStack.asCraftMirror(net.minecraft.server.v1_13_R2.
                ItemStack.a((NBTTagCompound) tagObj));
    }

    @Override
    protected Object saveToNBT(ItemStack item) {
        net.minecraft.server.v1_13_R2.ItemStack nmsItem = getNmsItemStack(item);
        return nmsItem.save(new NBTTagCompound());
    }

    @Override
    protected void damageItem(ItemStack item, int amount, LivingEntity livingEntity) {

        net.minecraft.server.v1_13_R2.ItemStack nmsItem = getNmsItemStack(item);
        int originalDamage = nmsItem.getDamage();
        nmsItem.damage(amount, ((CraftLivingEntity) livingEntity).getHandle());
        if (!isCraftItem(item)) {

            int newDamage = nmsItem.getDamage();
            if (originalDamage != newDamage) {
                ItemMeta meta = item.getItemMeta();
                ((Damageable) meta).setDamage(newDamage);
                item.setItemMeta(meta);
            }
        }
    }
}
