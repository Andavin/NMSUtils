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

import com.andavin.reflect.Reflection;
import com.andavin.util.LongHash;
import com.andavin.util.PacketSender;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @since May 28, 2018
 * @author Andavin
 */
public final class ChunkVisual {

    private static final int MAX_SNAPSHOTS = 10;
    private static final Field CHUNK, M_BLOCK_DATA, POSITION, S_BLOCK_DATA;
    private static final Constructor<?> BLOCK_POS, M_PACKET, S_PACKET, MULTI_BLOCK, CHUNK_PAIR;

    static {
        final Class<?> blockData = Reflection.getMcClass("IBlockData");
        final Class<?> singlePacket = Reflection.getMcClass("PacketPlayOutBlockChange");
        final Class<?> multiPacket = Reflection.getMcClass("PacketPlayOutMultiBlockChange");
        final Class<?> multiBlock = Reflection.getMcClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
        BLOCK_POS = Reflection.getConstructor(Reflection.getMcClass("BlockPosition"), int.class, int.class, int.class);
        CHUNK_PAIR = Reflection.getConstructor(Reflection.getMcClass("ChunkCoordIntPair"), int.class, int.class);
        M_PACKET = Reflection.getConstructor(multiPacket);
        MULTI_BLOCK = Reflection.getConstructor(multiBlock, multiPacket, short.class, blockData);
        S_PACKET = Reflection.getConstructor(singlePacket);
        CHUNK = Reflection.getField(multiPacket, "a");
        M_BLOCK_DATA = Reflection.getField(multiPacket, "b");
        POSITION = Reflection.getField(singlePacket, "a");
        S_BLOCK_DATA = Reflection.getField(singlePacket, "block");
    }

    private final int x, z;
    private final long chunk;
    private final Object chunkPair;
    private final Map<Short, VisualBlock> blocks = new ConcurrentHashMap<>();
    private final LinkedList<List<VisualBlock>> snapshots = new LinkedList<>();

    ChunkVisual(final long chunk) {
        this.chunk = chunk;
        this.x = LongHash.msw(chunk);
        this.z = LongHash.lsw(chunk);
        //noinspection ConstantConditions
        this.chunkPair = Reflection.getInstance(CHUNK_PAIR, this.x, this.z);
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
    public void addBlock(final VisualBlock block) {
        this.blocks.put(block.getPackedPos(), block);
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
     *
     * @param visual The {@link AreaVisual} that this chunk is part of.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public void revert(final AreaVisual visual) {
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
    public void revert(final int amount, final AreaVisual visual) {

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
            final Map<Long, VisualBlock> allBlocks = new HashMap<>();
            visual.getChunks().forEach(chunk -> chunk.blocks.values()
                    .forEach(block -> allBlocks.put(block.getId(), block)));

            for (final VisualBlock block : snapshot) { // Iterate the snapshot

                // Now we just need to find where each snapshot block is located
                final VisualBlock toRevert = allBlocks.get(block.getId()); // The block that needs to be reverted
                if (toRevert == null) {
                    // Couldn't find the block anywhere so it was removed (no reverting)
                    continue;
                }

                final VisualBlock reverted = new VisualBlock(toRevert.getId(),
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
    public void setType(final Material fromType, final Material toType) {
        this.setType(fromType, -1, toType, 0);
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
    public void setType(final Material fromType, final Material toType, final int toData) {
        this.setType(fromType, -1, toType, toData);
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
    public void setType(final Material fromType, final int fromData, final Material toType) {
        this.setType(fromType, fromData, toType, 0);
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
    public synchronized void setType(final Material fromType, final int fromData, final Material toType, final int toData) {

        this.snapshots.addFirst(new ArrayList<>(this.blocks.values()));
        if (this.snapshots.size() > MAX_SNAPSHOTS) {
            this.snapshots.removeLast();
        }

        this.blocks.entrySet().forEach(entry -> {

            final VisualBlock block = entry.getValue();
            // If type is null only match data if data is not -1
            if ((fromType == null || block.getType() == fromType) && (fromData == -1 || block.getData() == fromData)) {
                entry.setValue(new VisualBlock(block.getId(), block.getX(), block.getY(), block.getZ(), toType, toData));
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
    public List<VisualBlock> shift(final BlockFace direction) {
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
    public List<VisualBlock> shift(final int distance, final BlockFace direction) {
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
    public List<VisualBlock> shift(final int x, final int y, final int z) {
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
    public List<VisualBlock> rotateX(final Vector origin, final float degrees) {
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
    public List<VisualBlock> rotateY(final Vector origin, final float degrees) {
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
    public List<VisualBlock> rotateZ(final Vector origin, final float degrees) {
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
    public List<VisualBlock> transform(final Function<VisualBlock, VisualBlock> transformer) {

        final List<VisualBlock> blocks = new LinkedList<>(this.blocks.values());
        this.blocks.clear();
        final ListIterator<VisualBlock> itr = blocks.listIterator();
        while (itr.hasNext()) {

            final VisualBlock transformed = transformer.apply(itr.next());
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
    public void visualize(final Player player) {

        if (!this.blocks.isEmpty() && this.isLoaded(player.getWorld())) {
            this.sendPacket(player, new ArrayList<>(this.blocks.values()));
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
    public void visualize(final Player player, final Set<VisualBlock> snapshot) {

        /*
         * Three different kinds of blocks that need updated:
         * 1. Blocks that changed type
         * 2. Blocks removed
         * 3. Blocks added
         */

        final World world = player.getWorld();
        if (!this.isLoaded(world)) {
            // No need to update if this chunk isn't loaded
            return;
        }

        final ChunkSnapshot chunk = world.getChunkAt(this.x, this.z).getChunkSnapshot();
        final List<VisualBlock> needsUpdate = new LinkedList<>();
        snapshot.forEach(block -> {

            final VisualBlock current = this.blocks.get(block.getPackedPos());
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
            this.sendPacket(player, needsUpdate);
        }
    }

    /**
     * Reset all of the {@link VisualBlock blocks} for the given
     * player to the actual blocks that are in the world.
     *
     * @param player The player to clear the blocks for.
     */
    public void reset(final Player player) {

        if (!this.blocks.isEmpty() && this.isLoaded(player.getWorld())) {

            if (this.blocks.size() == 1) {
                this.sendPacket(player, Collections.singletonList(this.blocks.values()
                        .iterator().next().getRealType(player.getWorld())));
                return;
            }

            final ChunkSnapshot chunk = player.getWorld().getChunkAt(this.x, this.z).getChunkSnapshot();
            final List<VisualBlock> blocks = this.blocks.values().stream()
                    .map(block -> block.getRealType(chunk)).collect(Collectors.toList());
            this.sendPacket(player, blocks);
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
    public boolean equals(final Object obj) {
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

    private void sendPacket(final Player player, final List<VisualBlock> blocks) {

        // If there is only a single block to send then send
        // a single PacketPlayOutBlockChange packet
        if (blocks.size() == 1) {
            final VisualBlock block = blocks.get(0);
            final Object packet = Reflection.getInstance(S_PACKET);
            Reflection.setValue(POSITION, packet, Reflection.getInstance(BLOCK_POS,
                    block.getX(), block.getY(), block.getZ())); // Set the position
            Reflection.setValue(S_BLOCK_DATA, packet, block.getBlockData()); // And the data
            PacketSender.sendPacket(player, packet);
            return;
        }

        // There are multiple here so send a PacketPlayOutMultiBlockChange packet
        final Object packet = Reflection.getInstance(M_PACKET);
        final Object[] blockData = (Object[]) Array.newInstance(MULTI_BLOCK.getDeclaringClass(), blocks.size());
        Reflection.setValue(CHUNK, packet, this.chunkPair); // Set the chunk that it is in
        Reflection.setValue(M_BLOCK_DATA, packet, blockData); // Place the array into the packet

        // Then update the array with all of the data and positions
        int i = 0;
        for (final VisualBlock block : blocks) {
            blockData[i++] = Reflection.getInstance(MULTI_BLOCK, packet, block.getPackedPos(), block.getBlockData());
        }

        // Send the packet to the player
        PacketSender.sendPacket(player, packet);
    }

    private boolean isLoaded(final World world) {
        return world.isChunkLoaded(this.x, this.z);
    }
}
