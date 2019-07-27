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

package com.andavin.v1_14_R1.visual;

import com.andavin.visual.block.VisualBlock;
import net.minecraft.server.v1_14_R1.*;
import net.minecraft.server.v1_14_R1.PacketPlayOutMultiBlockChange.MultiBlockChangeInfo;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rail;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.bukkit.material.ExtendedRails;
import org.bukkit.material.Rails;
import org.bukkit.material.Tree;

import java.lang.reflect.Field;
import java.util.List;

import static com.andavin.protocol.PacketSender.sendPacket;
import static com.andavin.reflect.Reflection.findField;
import static com.andavin.reflect.Reflection.setFieldValue;

/**
 * @since November 15, 2018
 * @author Andavin
 */
class VisualBridge extends com.andavin.visual.VisualBridge {

    private static final Field CHUNK = findField(PacketPlayOutMultiBlockChange.class, "a");
    private static final Field PACKET_BLOCK_DATA = findField(PacketPlayOutMultiBlockChange.class, "b");
    private static final Field POSITION = findField(PacketPlayOutBlockChange.class, "a");

    @Override
    protected void sendBlocks(Player player, Object chunkPair, List<VisualBlock> blocks) {

        if (blocks.isEmpty()) {
            return;
        }

        // If there is only a single block to send then send
        // a single PacketPlayOutBlockChange packet
        if (blocks.size() == 1) {
            VisualBlock block = blocks.get(0);
            PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange();
            setFieldValue(POSITION, packet, new BlockPosition(block.getX(), block.getY(), block.getZ())); // Set the position
            packet.block = toData(block); // And the data
            sendPacket(player, packet);
            return;
        }

        // There are multiple here so send a PacketPlayOutMultiBlockChange packet
        PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
        MultiBlockChangeInfo[] blockData = new MultiBlockChangeInfo[blocks.size()];

        setFieldValue(CHUNK, packet, chunkPair); // Set the chunk that it is in
        setFieldValue(PACKET_BLOCK_DATA, packet, blockData); // Place the array into the packet
        // Then update the array with all of the data and positions
        int i = 0;
        for (VisualBlock block : blocks) {
            blockData[i++] = packet.new MultiBlockChangeInfo(block.getPackedPosition(), toData(block));
        }

        // Send the packet to the player
        sendPacket(player, packet);
    }

    @Override
    protected Object createChunkCoordIntPair(int x, int z) {
        return new ChunkCoordIntPair(x, z);
    }

    @Override
    public VisualBlock getRealType(VisualBlock original, ChunkSnapshot snapshot, int x, int y, int z) {
        BlockData data = snapshot.getBlockData(x, y, z);
        return new VisualBlock(original.getX(), original.getY(), original.getZ(), data.getMaterial());
    }

    @Override
    public boolean isDirectional(Material type) {
        Class<?> data = type.data;
        return type.isLegacy() ?
                org.bukkit.material.Directional.class.isAssignableFrom(data) ||
                        Tree.class.isAssignableFrom(data) ||
                        Rails.class.isAssignableFrom(data) ||
                        ExtendedRails.class.isAssignableFrom(data) :
                Directional.class.isAssignableFrom(data) ||
                        Rail.class.isAssignableFrom(data);
    }

    private IBlockData toData(VisualBlock block) {
        return ((CraftBlockData) block.getBlockData()).getState();
    }
}
