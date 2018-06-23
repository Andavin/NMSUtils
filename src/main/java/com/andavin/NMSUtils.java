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

import com.andavin.nbt.wrapper.*;
import com.andavin.util.LocationUtil;
import com.andavin.visual.AreaVisual;
import com.andavin.visual.VisualBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class NMSUtils extends JavaPlugin implements Listener {

    private static NMSUtils instance;

    private boolean added;
    private int typeIndex;
    private final AreaVisual visual = new AreaVisual();
    private static final Material[] TYPES = {
            Material.WOOD, Material.WOOL, Material.WATER
    };

    @Override
    public void onEnable() {
        instance = this;
        //noinspection unchecked
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
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.visual.shift(LocationUtil.getCardinalDirection(event.getPlayer().getLocation()));
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (this.added && this.typeIndex > 0) {
                this.visual.revert();
                this.typeIndex--;
                return;
            }

            final Location loc = event.getPlayer().getLocation().add(5, 0, 0);
            final List<VisualBlock> blocks = new ArrayList<>(4096);
            for (int x = 0; x < 16; x++) {

                for (int y = 0; y < 16; y++) {

                    for (int z = 0; z < 16; z++) {
                        blocks.add(new VisualBlock(x + loc.getBlockX(), y + loc.getBlockY(),
                                z + loc.getBlockZ(), Material.MELON_BLOCK));
                    }
                }
            }

            this.visual.addBlock(blocks).visualize(event.getPlayer());
            this.added = true;
            return;
        }

        if (this.typeIndex < TYPES.length) {
            this.visual.setType(null, TYPES[this.typeIndex++]);
        }
    }

    /**
     * The singleton instance of the {@link NMSUtils} plugin.
     *
     * @return This plugin instance.
     */
    public static NMSUtils getInstance() {
        return instance;
    }
}
