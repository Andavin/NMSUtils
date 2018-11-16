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

package com.andavin.visual;

import com.andavin.Versioned;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @since November 15, 2018
 * @author Andavin
 */
public abstract class VisualBridge extends Versioned {

    /**
     * Get the list of {@code IBlockData} mapped to their IDs
     * in a list contained within the {@code RegistryBlockID}
     * class within {@code Block}.
     * <p>
     * This is used to get the instance of {@code IBlockData}
     * from block IDs.
     *
     * @return The ID list for blocks.
     */
    protected abstract List<Object> getRegistryList();

    /**
     * Send a block change packet to the given player for all of
     * the blocks contained within the list.
     * <p>
     * Note that all blocks contained within the list must be inside
     * of the given chunk or else unexpected behavior may occur.
     *
     * @param player The player to send the packet to.
     * @param chunkPair The {@code ChunkCoordIntPair} of the chunk
     *                  that the blocks belong to.
     * @param blocks The block changes to send to the player.
     */
    protected abstract void sendBlocks(Player player, Object chunkPair, List<VisualBlock> blocks);

    /**
     * Create a new {@code ChunkCoordIntPair} for the given
     * chunk coordinates.
     *
     * @param x The X coordinate of the chunk.
     * @param z The Z coordinate of the chunk.
     * @return The newly created object.
     */
    protected abstract Object createChunkCoordIntPair(int x, int z);
}
