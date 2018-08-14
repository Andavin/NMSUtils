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

package com.andavin;

import com.andavin.chat.Strings;
import com.andavin.nbt.wrapper.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class NMSUtils extends JavaPlugin implements Listener {

    private static NMSUtils instance;
    private static boolean fastAsyncSupport;

    public NMSUtils() {
        instance = this;
        NBTHelper.register(
                NBTTagEnd.class,
                NBTTagByte.class,
                NBTTagShort.class,
                NBTTagInt.class,
                NBTTagLong.class,
                NBTTagFloat.class,
                NBTTagDouble.class,
                NBTTagByteArray.class,
                NBTTagIntArray.class,
                NBTTagLongArray.class,
                NBTTagCompound.class,
                NBTTagList.class,
                NBTTagString.class
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            final ItemStack item = new ItemStack(Material.STONE);
            final ItemMeta meta = item.getItemMeta();

            final List<String> lore = new ArrayList<>(Arrays.asList(
                    "§fOne side of a line - And another",
                    "§fA second - More detail on this side",
                    "§fA third line - with a dash",
                    "§fAnd a last line that - is off from others"
            ));

            final int line = Strings.getLongestLine(lore);
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, Strings.centerOn(lore.get(i), '-', line));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
            event.getPlayer().getInventory().addItem(item);
        }
    }

    @Override
    public void onEnable() {
        fastAsyncSupport = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    /**
     * The singleton instance of the {@link NMSUtils} plugin.
     *
     * @return This plugin instance.
     */
    public static NMSUtils getInstance() {
        return instance;
    }

    /**
     * Tell if FastAsyncWorldEdit is available for use.
     *
     * @return If FastAsyncWorldEdit is supported.
     */
    public static boolean fastAsyncSupport() {
        return fastAsyncSupport;
    }
}
