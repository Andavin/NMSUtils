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
import com.andavin.visual.block.VisualBlock;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @since November 15, 2018
 * @author Andavin
 */
public abstract class VisualBridge implements Versioned {

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

    /**
     * Get a VisualBlock for the block that is actually in the
     * {@link ChunkSnapshot} at the location of this block.
     *
     * @param original The block to get the real type for.
     * @param snapshot A snapshot of the chunk that this block is contained within.
     * @param x The X coordinate of the block relative to the chunk snapshot (0-15).
     * @param y The Y coordinate of the block (0-255).
     * @param z The Z coordinate of the block relative to the chunk snapshot (0-15).
     * @return A new block of the type of the actual block at this block's location.
     */
    public abstract VisualBlock getRealType(VisualBlock original, ChunkSnapshot snapshot, int x, int y, int z);

    /**
     * Tell if the given {@link Material} is a directional
     * type that can be rotated about itself to face different
     * directions or {@link BlockFace}s.
     *
     * @param type The type to test for being directional.
     * @return If the type is directional.
     */
    public abstract boolean isDirectional(Material type);

//    protected abstract byte rotateX(float degrees, byte data, Object blockData);
//
//    protected abstract byte rotateY(float degrees, byte data, Object blockData);
//
//    protected abstract byte rotateZ(float degrees, byte data, Object blockData);
}
