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

import com.andavin.util.LongHash;
import com.andavin.util.PacketSender;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.andavin.reflect.Reflection.*;

/**
 * @since May 28, 2018
 * @author Andavin
 */
public final class ChunkVisual {

    private static final int MAX_SNAPSHOTS = 10;
    private static final Field CHUNK, M_BLOCK_DATA, POSITION, S_BLOCK_DATA;
    private static final Constructor<?> BLOCK_POS, M_PACKET, S_PACKET, MULTI_BLOCK, CHUNK_PAIR;

    static {
        Class<?> blockData = findMcClass("IBlockData");
        Class<?> singlePacket = findMcClass("PacketPlayOutBlockChange");
        Class<?> multiPacket = findMcClass("PacketPlayOutMultiBlockChange");
        Class<?> multiBlock = findMcClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
        BLOCK_POS = findConstructor(findMcClass("BlockPosition"), int.class, int.class, int.class);
        CHUNK_PAIR = findConstructor(findMcClass("ChunkCoordIntPair"), int.class, int.class);
        M_PACKET = findConstructor(multiPacket);
        MULTI_BLOCK = findConstructor(multiBlock, multiPacket, short.class, blockData);
        S_PACKET = findConstructor(singlePacket);
        CHUNK = findField(multiPacket, "a");
        M_BLOCK_DATA = findField(multiPacket, "b");
        POSITION = findField(singlePacket, "a");
        S_BLOCK_DATA = findField(singlePacket, "block");
    }

    private final int x, z;
    private final long chunk;
    private final Object chunkPair;
    private final Map<Short, VisualBlock> blocks = new ConcurrentHashMap<>();
    private final LinkedList<List<VisualBlock>> snapshots = new LinkedList<>();

    ChunkVisual(long chunk) {
        this.chunk = chunk;
        this.x = LongHash.msw(chunk);
        this.z = LongHash.lsw(chunk);
        this.chunkPair = newInstance(CHUNK_PAIR, this.x, this.z);
    }

    /**
     * Get the X coordinate of this chunk. This is a chunk
     * coordinate, so 1/16th of the coordinates of the blocks
     * contained within the chunk.
     *
     * @return This chunk's X coordinate.
     * @see LongHash
     */
    public int getX() {
        return x;
    }

    /**
     * Get the Z coordinate of this chunk. This is a chunk
     * coordinate, so 1/16th of the coordinates of the blocks
     * contained within the chunk.
     *
     * @return This chunk's Z coordinate.
     * @see LongHash
     */
    public int getZ() {
        return z;
    }

    /**
     * Get the hashed {@code long} coordinates of this
     * visualized chunk.
     *
     * @return The {@code long} hash for this chunk.
     * @see LongHash
     */
    long getChunk() {
        return chunk;
    }

    /**
     * Get a snapshot the current blocks in this chunk.
     * This will be the same {@link VisualBlock block}
     * objects that are currently in the chunk, but in
     * a new collection.
     *
     * @return A new {@link Set} of the blocks in this chunk.
     */
    Set<VisualBlock> snapshot() {
        return new HashSet<>(this.blocks.values());
    }

    /**
     * Tell if this chunk has no blocks currently
     * able to be visualized in it.
     *
     * @return If there are no blocks in this chunk.
     */
    boolean isEmpty() {
        return this.blocks.isEmpty() && this.snapshots.isEmpty();
    }

    /**
     * Add a new block to this chunk. If the block is already
     * present in this chunk then it will be replaced and sent
     * to the clients on the next update.
     *
     * @param block The {@link VisualBlock block} to add.
     */
    public void addBlock(VisualBlock block) {
        this.blocks.put(block.getPackedPos(), block);
    }

    /**
     * Remove the {@link VisualBlock block} at the given coordinates.
     * If the block coordinates are not contained within this chunk
     * or there is no block at the coordinates, then this method will
     * do nothing.
     *
     * @param x The X coordinate of the block to remove.
     * @param y The Y coordinate of the block to remove.
     * @param z The Z coordinate of the block to remove.
     * @return The VisualBlock previously at the given coordinates or
     *         {@code null} if there is no block at the coordinates.
     */
    public VisualBlock removeBlock(int x, int y, int z) {

        if (x >> 4 != this.x || z >> 4 != this.z) {
            return null;
        }

        return this.blocks.remove((short) ((x & 0xF) << 12 | (z & 0xF) << 8 | y & 0xFF));
    }

    /**
     * Get the {@link VisualBlock block} at the given coordinates.
     * If the block coordinates are not contained within this chunk
     * or there is no block at the coordinates, then {@code null}
     * will be returned.
     *
     * @param x The X coordinate of the block to retrieve.
     * @param y The Y coordinate of the block to retrieve.
     * @param z The Z coordinate of the block to retrieve.
     * @return The VisualBlock at the given coordinates or {@code null}
     *         if there is no block at the coordinates.
     */
    @Nullable
    public VisualBlock getBlock(int x, int y, int z) {

        if (x >> 4 != this.x || z >> 4 != this.z) {
            return null;
        }

        return this.blocks.get((short) ((x & 0xF) << 12 | (z & 0xF) << 8 | y & 0xFF));
    }

    /**
     * Revert a change to this chunk that took place via one of the
     * {@code setType()} methods. This will completely destroy the
     * current block state of this chunk and replace it with the
     * last snapshot taken.
     * <p>
     * If there are no snapshots to revert to, then this method will
     * do nothing.
     * <p>
     * Currently, only the {@code setType()} methods support reverting
     * to snapshots. Transformations may be implemented in a future version.
     * <p>
     * Note that this method will still require a visualization refresh
     * of some kind in order to show the blocks to players. It will not
     * execute a refresh automatically.
     * <p>
     * Note that there is an inherent flaw with this method when combined
     * with any rotation methods:
     * <ul>
     *     <li>{@link #rotateX(Vector, float)}</li>
     *     <li>{@link #rotateY(Vector, float)}</li>
     *     <li>{@link #rotateZ(Vector, float)}</li>
     * </ul>
     * If an area is {@link #setType(Material, int, Material, int) changed type},
     * rotated (using the aforementioned methods) and then reverted (using
     * any revert method), then any directional types that were changed type
     * must revert to their previous rotation before the type change occurred.
     * <p>
     * There are a couple ways of going around this flaw:
     * <ol>
     *     <li>Simply ignore all directional blocks during the type change via
     *     the {@code ignoreDirectional} parameter.</li>
     *     <li>Keep a snapshot of the area before the type change and after reverting
     *     rotate all of the directional blocks the combined rotation of any rotations
     *     that have occurred, if any.</li>
     * </ol>
     * Out of the two options the first is the easiest while the second is the
     * best outcome. This cannot happen internally, as of now, due to there being
     * no reference from which to gain access to the new rotation of the block after revert.
     *
     * @param visual The {@link AreaVisual} that this chunk is part of.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public void revert(AreaVisual visual) {
        this.revert(1, visual);
    }

    /**
     * Revert a change to this chunk that took place via one of the
     * {@code setType()} methods. This will completely destroy the
     * current block state of this chunk and replace it with the
     * last snapshot taken.
     * <p>
     * If the amount of snapshots to revert back is higher than the
     * amount of snapshots available, then the oldest snapshot will
     * be chosen to revert to. On the other hand, if there are no snapshots
     * or the amount given is less than {@code 1}, then the revert will
     * do nothing.
     * <p>
     * Currently, only the {@code setType()} methods support reverting
     * to snapshots. Transformations may be implemented in a future version.
     * <p>
     * Note that this method will still require a visualization refresh
     * of some kind in order to show the blocks to players. It will not
     * execute a refresh automatically.
     * <p>
     * Note that there is an inherent flaw with this method when combined
     * with any rotation methods:
     * <ul>
     *     <li>{@link #rotateX(Vector, float)}</li>
     *     <li>{@link #rotateY(Vector, float)}</li>
     *     <li>{@link #rotateZ(Vector, float)}</li>
     * </ul>
     * If an area is {@link #setType(Material, int, Material, int) changed type},
     * rotated (using the aforementioned methods) and then reverted (using
     * any revert method), then any directional types that were changed type
     * must revert to their previous rotation before the type change occurred.
     * <p>
     * There are a couple ways of going around this flaw:
     * <ol>
     *     <li>Simply ignore all directional blocks during the type change via
     *     the {@code ignoreDirectional} parameter.</li>
     *     <li>Keep a snapshot of the area before the type change and after reverting
     *     rotate all of the directional blocks the combined rotation of any rotations
     *     that have occurred, if any.</li>
     * </ol>
     * Out of the two options the first is the easiest while the second is the
     * best outcome. This cannot happen internally, as of now, due to there being
     * no reference from which to gain access to the new rotation of the block after revert.
     *
     * @param amount The amount of snapshots to revert back to. For example,
     *               if there have been {@code 4} snapshots taken and {@code 3}
     *               is given, then the 3rd snapshot will be the one reverted to.
     * @param visual The {@link AreaVisual} that this chunk is part of.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public void revert(int amount, AreaVisual visual) {

        if (this.snapshots.isEmpty() || amount < 1) {
            return;
        }

        List<VisualBlock> snapshot = null;
        if (this.snapshots.size() < amount) {
            snapshot = this.snapshots.removeLast();
            this.snapshots.clear();
        } else if (amount == 1) {
            snapshot = this.snapshots.removeFirst();
        } else {
            // Two or more reverts
            for (int i = 0; i < amount; i++) {
                snapshot = this.snapshots.removeFirst();
            }
        }

        if (snapshot != null) {

            // Load up all of the blocks in other chunks and this chunk
            // These will all have unique IDs that can be referenced
            ChunkVisual currentChunk = null;
            Map<Long, VisualBlock> allBlocks = new HashMap<>();
            visual.getChunks().forEach(chunk -> chunk.blocks.values()
                    .forEach(block -> allBlocks.put(block.getId(), block)));

            for (VisualBlock block : snapshot) { // Iterate the snapshot

                // Now we just need to find where each snapshot block is located
                VisualBlock toRevert = allBlocks.get(block.getId()); // The block that needs to be reverted
                if (toRevert == null) {
                    // Couldn't find the block anywhere so it was removed (no reverting)
                    continue;
                }

                VisualBlock reverted = new VisualBlock(toRevert.getId(),
                        toRevert.getX(), toRevert.getY(), toRevert.getZ(), block.getType(), block.getData());
                if (toRevert.getChunk() == this.chunk) {
                    // Found the block in this chunk so revert it and move on
                    this.blocks.put(toRevert.getPackedPos(), reverted);
                    continue;
                }

                // Must revert the block in an alternate chunk because
                // it must have been transformed to another chunk
                // Get the chunk that the block is in so we can revert the block there
                if (currentChunk == null || currentChunk.chunk != toRevert.getChunk()) {

                    currentChunk = visual.getChunk(LongHash.msw(toRevert.getChunk()),
                            LongHash.lsw(toRevert.getChunk()));
                    if (currentChunk == null || currentChunk.isEmpty()) {
                        continue;
                    }
                }

                // Revert the block in the alternate chunk
                currentChunk.blocks.put(toRevert.getPackedPos(), reverted);
            }
        }
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     */
    public void setType(Material fromType, Material toType) {
        this.setType(fromType, -1, toType, 0);
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @see #revert(AreaVisual) revert
     */
    public void setType(Material fromType, Material toType, boolean ignoreDirectional) {
        this.setType(fromType, -1, toType, 0, ignoreDirectional);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     */
    public void setType(Material fromType, Material toType, int toData) {
        this.setType(fromType, -1, toType, toData);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @see #revert(AreaVisual) revert
     */
    public void setType(Material fromType, Material toType, int toData, boolean ignoreDirectional) {
        this.setType(fromType, -1, toType, toData, ignoreDirectional);
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type and data criteria set.
     * <p>
     * The data criteria ({@code fromData}) can be set to {@code -1}
     * in order to disable it and change any block as long as the
     * type matches; the {@link #setType(Material, Material)} or
     * {@link #setType(Material, Material, int)} can also be used.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param fromData The {@link VisualBlock#getData() data} of the
     *                 blocks to change to the new type.
     * @param toType The type to change the matching blocks to.
     */
    public void setType(Material fromType, int fromData, Material toType) {
        this.setType(fromType, fromData, toType, 0);
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type and data criteria set.
     * <p>
     * The data criteria ({@code fromData}) can be set to {@code -1}
     * in order to disable it and change any block as long as the
     * type matches; the {@link #setType(Material, Material)} or
     * {@link #setType(Material, Material, int)} can also be used.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param fromData The {@link VisualBlock#getData() data} of the
     *                 blocks to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @see #revert(AreaVisual) revert
     */
    public void setType(Material fromType, int fromData, Material toType, boolean ignoreDirectional) {
        this.setType(fromType, fromData, toType, 0, ignoreDirectional);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type and data
     * criteria set.
     * <p>
     * The data criteria ({@code fromData}) can be set to {@code -1}
     * in order to disable it and change any block as long as the
     * type matches; the {@link #setType(Material, Material)} or
     * {@link #setType(Material, Material, int)} can also be used.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param fromData The {@link VisualBlock#getData() data} of the
     *                 blocks to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     */
    public void setType(Material fromType, int fromData, Material toType, int toData) {
        this.setType(fromType, fromData, toType, toData, false);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type and data
     * criteria set.
     * <p>
     * The data criteria ({@code fromData}) can be set to {@code -1}
     * in order to disable it and change any block as long as the
     * type matches; the {@link #setType(Material, Material)} or
     * {@link #setType(Material, Material, int)} can also be used.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param fromData The {@link VisualBlock#getData() data} of the
     *                 blocks to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @see #revert(AreaVisual) revert
     */
    public synchronized void setType(Material fromType, int fromData,
                                     Material toType, int toData, boolean ignoreDirectional) {

        List<VisualBlock> changed = new LinkedList<>();
        this.snapshots.addFirst(changed);
        if (this.snapshots.size() > MAX_SNAPSHOTS) {
            this.snapshots.removeLast();
        }

        this.blocks.entrySet().forEach(entry -> {

            VisualBlock block = entry.getValue();
            if (ignoreDirectional && block.isDirectional()) {
                return;
            }

            // If type is null only match data if data is not -1
            if ((fromType == null || block.getType() == fromType) && (fromData == -1 || block.getData() == fromData)) {
                entry.setValue(new VisualBlock(block.getId(), block.getX(), block.getY(), block.getZ(), toType, toData));
                changed.add(block);
            }
        });
    }

    /**
     * Shift all of the blocks that are contained in this chunk
     * to be visualized a single block in a direction.
     * <p>
     * If all of the blocks, once shifted, do not fit within this
     * chunk anymore, then all of the blocks that are not in this
     * chunk will be returned as an overflow list.
     * <p>
     * Note that it is suggested to invoke {@link #reset(Player)}
     * on this chunk before calling this method in order to remove
     * all blocks that are previously visualized as this method will
     * replace all old blocks with the new shifted ones.
     *
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> shift(BlockFace direction) {
        return this.transform(block -> block.shift(direction));
    }

    /**
     * Shift all of the blocks that are contained in this chunk
     * to be visualized a certain amount of blocks in a direction.
     * <p>
     * If all of the blocks, once shifted, do not fit within this
     * chunk anymore, then all of the blocks that are not in this
     * chunk will be returned as an overflow list.
     * <p>
     * Note that it is suggested to invoke {@link #reset(Player)}
     * on this chunk before calling this method in order to remove
     * all blocks that are previously visualized as this method will
     * replace all old blocks with the new shifted ones.
     *
     * @param distance The distance (in blocks) to shift.
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> shift(int distance, BlockFace direction) {
        return this.transform(block -> block.shift(distance, direction));
    }

    /**
     * Shift all of the blocks that are contained in this chunk
     * to be visualized a certain amount of blocks along each of
     * the x, y and z axes.
     * <p>
     * If all of the blocks, once shifted, do not fit within this
     * chunk anymore, then all of the blocks that are not in this
     * chunk will be returned as an overflow list.
     * <p>
     * Note that it is suggested to invoke {@link #reset(Player)}
     * on this chunk before calling this method in order to remove
     * all blocks that are previously visualized as this method will
     * replace all old blocks with the new shifted ones.
     *
     * @param x The amount of blocks to shift along the x-axis.
     * @param y The amount of blocks to shift along the y-axis.
     * @param z The amount of blocks to shift along the z-axis.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> shift(int x, int y, int z) {
        return this.transform(block -> block.shift(x, y, z));
    }

    /**
     * Rotate all of the blocks that are contained in this chunk to be
     * visualized the specified amount of degrees around the X-axis.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the X axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * west toward negative X.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> rotateX(Vector origin, float degrees) {
        return this.transform(block -> block.rotateX(origin, degrees));
    }

    /**
     * Rotate all of the blocks that are contained in this chunk to be
     * visualized the specified amount of degrees around the Y-axis.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the Y axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * down toward negative Y.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> rotateY(Vector origin, float degrees) {
        return this.transform(block -> block.rotateY(origin, degrees));
    }

    /**
     * Rotate all of the blocks that are contained in this chunk to be
     * visualized the specified amount of degrees around the Z-axis.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the Z axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * north toward negative Z.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> rotateZ(Vector origin, float degrees) {
        return this.transform(block -> block.rotateZ(origin, degrees));
    }

    /**
     * Transform this chunk by using the given {@link Function transformer}
     * on each {@link VisualBlock} that is contained within this chunk.
     * <p>
     * This will execute the transformer using each block in this chunk
     * and replace the block in the chunk if it is still contained within
     * or else it will add it to the list of blocks that cannot fit in this
     * chunk anymore.
     *
     * @param transformer The function to use on each block to transform it.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public List<VisualBlock> transform(Function<VisualBlock, VisualBlock> transformer) {

        List<VisualBlock> blocks = new LinkedList<>(this.blocks.values());
        this.blocks.clear();
        ListIterator<VisualBlock> itr = blocks.listIterator();
        while (itr.hasNext()) {

            VisualBlock transformed = transformer.apply(itr.next());
            if (transformed.getChunk() == this.chunk) { // Make sure it is still in this chunk
                this.blocks.put(transformed.getPackedPos(), transformed);
                itr.remove();
                continue;
            }

            itr.set(transformed);
        }

        return blocks;
    }

    /**
     * Send all of the fake block types to the given {@link Player}.
     *
     * @param player The player to send the {@link VisualBlock blocks} to.
     */
    public void visualize(Player player) {

        if (!this.blocks.isEmpty() && this.isLoaded(player.getWorld())) {
            this.sendBlocks(player, new ArrayList<>(this.blocks.values()));
        }
    }

    /**
     * Send all of the fake block types to the given {@link Player},
     * but only update blocks that must be updated. If the block is
     * the same type and still exists after the previous snapshot,
     * then it will not be updated.
     *
     * @param player The player to send the {@link VisualBlock blocks} to.
     * @param snapshot The {@link #snapshot() snapshot} of a previous
     *                 version of this chunk.
     */
    public void visualize(Player player, Set<VisualBlock> snapshot) {

        /*
         * Three different kinds of blocks that need updated:
         * 1. Blocks that changed type
         * 2. Blocks removed
         * 3. Blocks added
         */

        World world = player.getWorld();
        if (!this.isLoaded(world)) {
            // No need to update if this chunk isn't loaded
            return;
        }

        ChunkSnapshot chunk = world.getChunkAt(this.x, this.z).getChunkSnapshot();
        List<VisualBlock> needsUpdate = new LinkedList<>();
        snapshot.forEach(block -> {

            VisualBlock current = this.blocks.get(block.getPackedPos());
            if (current == null) {
                // The block was removed
                needsUpdate.add(block.getRealType(chunk));
            } else if (current.getType() != block.getType() || current.getData() != block.getData()) {
                // The block type was changed
                needsUpdate.add(current);
            }
        });

        // Blocks that were added
        if (!this.blocks.isEmpty()) {
            this.blocks.values().stream().filter(block -> !snapshot.contains(block)).forEach(needsUpdate::add);
        }

        if (!needsUpdate.isEmpty()) {
            this.sendBlocks(player, needsUpdate);
        }
    }

    /**
     * Refresh a single {@link VisualBlock block} coordinate
     * for the given player. If the block is not contained within
     * this chunk, then this method will do nothing.
     *
     * @param player The player to refresh the block for.
     * @param x The X coordinate of the block to refresh.
     * @param y The Y coordinate of the block to refresh.
     * @param z The Z coordinate of the block to refresh.
     */
    public void refresh(Player player, int x, int y, int z) {

        VisualBlock block = this.getBlock(x, y, z);
        if (block != null) {
            this.sendBlocks(player, Collections.singletonList(block));
        }
    }

    /**
     * Reset all of the {@link VisualBlock blocks} for the given
     * player to the actual blocks that are in the world.
     *
     * @param player The player to clear the blocks for.
     */
    public void reset(Player player) {

        if (!this.blocks.isEmpty() && this.isLoaded(player.getWorld())) {

            if (this.blocks.size() == 1) {
                this.sendBlocks(player, Collections.singletonList(this.blocks.values()
                        .iterator().next().getRealType(player.getWorld())));
                return;
            }

            ChunkSnapshot chunk = player.getWorld().getChunkAt(this.x, this.z).getChunkSnapshot();
            List<VisualBlock> blocks = this.blocks.values().stream()
                    .map(block -> block.getRealType(chunk)).collect(Collectors.toList());
            this.sendBlocks(player, blocks);
        }
    }

    /**
     * Clear all of the blocks from this chunk. This will not
     * reset any blocks for any players and will in fact make
     * this chunk not able to reset any blocks until the blocks
     * are added back. The {@link #reset(Player)} method should
     * be invoked on all visualized players before this method.
     */
    public void clear() {
        this.blocks.clear();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && obj.getClass() == ChunkVisual.class
                && ((ChunkVisual) obj).chunk == chunk;
    }

    @Override
    public int hashCode() {
        return this.chunkPair.hashCode();
    }

    @Override
    public String toString() {
        return this.chunkPair.toString();
    }

    /**
     * Send a block change packet to the given player for all of
     * the blocks contained within the list.
     * <p>
     * Note that all blocks contained within the list must be inside
     * of this chunk or else unexpected behavior may occur.
     *
     * @param player The player to send the packet to.
     * @param blocks The block changes to send to the player.
     */
    public void sendBlocks(Player player, List<VisualBlock> blocks) {

        // TODO add a fail safe to check if the chunk is in view distance of the player and/or is loaded
        // If there is only a single block to send then send
        // a single PacketPlayOutBlockChange packet
        if (blocks.size() == 1) {
            VisualBlock block = blocks.get(0);
            Object packet = newInstance(S_PACKET);
            setValue(POSITION, packet, newInstance(BLOCK_POS,
                    block.getX(), block.getY(), block.getZ())); // Set the position
            setValue(S_BLOCK_DATA, packet, block.getBlockData()); // And the data
            PacketSender.sendPacket(player, packet);
            return;
        }

        // There are multiple here so send a PacketPlayOutMultiBlockChange packet
        Object packet = newInstance(M_PACKET);
        Object[] blockData = (Object[]) Array.newInstance(MULTI_BLOCK.getDeclaringClass(), blocks.size());
        setValue(CHUNK, packet, this.chunkPair); // Set the chunk that it is in
        setValue(M_BLOCK_DATA, packet, blockData); // Place the array into the packet

        // Then update the array with all of the data and positions
        int i = 0;
        for (VisualBlock block : blocks) {
            blockData[i++] = newInstance(MULTI_BLOCK, packet, block.getPackedPos(), block.getBlockData());
        }

        // Send the packet to the player
        PacketSender.sendPacket(player, packet);
    }

    private boolean isLoaded(World world) {
        return world.isChunkLoaded(this.x, this.z);
    }
}