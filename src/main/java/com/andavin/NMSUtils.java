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
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class NMSUtils extends JavaPlugin implements Listener {

    private static NMSUtils instance;

    private boolean added, locked;
    private final AreaVisual visual = new AreaVisual();

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
        this.locked = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR;
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {

        final Location from, to = event.getTo();
        final BlockFace fromFace, toFace = LocationUtil.getCardinalDirection(to);
        if (!this.added) {

            this.added = true;
            final byte data;
            switch (toFace.getOppositeFace()) {
                case DOWN:
                    data = 0x0;
                    break;

                case UP:
                    data = 0x1;
                    break;

                case NORTH:
                    data = 0x2;
                    break;

                case SOUTH:
                    data = 0x3;
                    break;

                case WEST:
                    data = 0x4;
                    break;

                case EAST:
                default:
                    data = 0x5;
            }

            final VisualBlock block = new VisualBlock(to.getBlockX(), to.getBlockY(), to.getBlockZ(),
                    Material.DISPENSER, data).shift(5, toFace);
            this.visual.addBlock(block).visualize(event.getPlayer());
            return;
        }

        if (!this.locked) {

            from = event.getFrom();
            final int x = to.getBlockX() - from.getBlockX();
            final int y = to.getBlockY() - from.getBlockY();
            final int z = to.getBlockZ() - from.getBlockZ();
            if (x != 0 || y != 0 || z != 0) {
                this.visual.shift(x, y, z);
            }

            fromFace = LocationUtil.getCardinalDirection(from);
            if (fromFace != toFace) {
                this.visual.rotateY(to.toVector(), LocationUtil.getDifference(fromFace, toFace));
            }
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
