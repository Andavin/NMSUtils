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
import com.andavin.visual.block.VisualBlock;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

/**
 * An area of blocks that are simply visual. This is
 * used to send fake blocks to {@link Player clients}
 * that will be easily updated back to what they were
 * formerly and never effect the server since it never
 * knows of their existence.
 *
 * @since May 28, 2018
 * @author Andavin
 * @see VisualBlock
 */
public final class AreaVisual {

    private final Map<Long, ChunkVisual> chunks = new ConcurrentHashMap<>();
    private final Map<UUID, WeakReference<Player>> visualized = new HashMap<>();

    /**
     * Get all of the players that have been visualized to using
     * this area visual. If any of the players have gone offline,
     * then they will not be included in the returned set.
     *
     * @return A set of all players that have been visualized to.
     */
    public Set<Player> getPlayers() {
        return this.visualized.isEmpty() ? Collections.emptySet() : this.visualized.values().stream()
                .map(WeakReference::get).filter(Objects::nonNull).collect(toSet());
    }

    /**
     * Run an action for each of the players that have been visualized
     * to using this area visual. If any of the players have gone offline,
     * then they will not be included in the execution.
     *
     * @param action The action to execute for each player.
     * @return This AreaVisual object.
     */
    public AreaVisual forEach(Consumer<Player> action) {

        if (!this.visualized.isEmpty()) {
            this.visualized.values().stream().map(WeakReference::get).filter(Objects::nonNull).forEach(action);
        }

        return this;
    }

    /**
     * Visualize and send all of the fake blocks to the given
     * {@link Player client}. This is essentially an update method.
     * Any blocks that are added via {@link #addBlock(VisualBlock)}
     * or {@link #addBlock(Collection)} will not be sent to the client
     * until this method is invoked. In addition, any subsequent
     * blocks that are added will not be updated until this method is
     * called again to update the blocks.
     * <p>
     * To revert the blocks, call {@link #clear()} which will remove
     * all of the blocks from all of the visualized clients if they
     * are still online.
     * <p>
     * Note that if the client is not in visual range of the blocks
     * that have been added to this area, the blocks will most likely
     * never be seen.
     *
     * @param player The client to show the fake blocks to.
     * @return This AreaVisual object.
     */
    public AreaVisual visualize(Player player) {

        synchronized (this.visualized) {
            this.visualized.put(player.getUniqueId(), new WeakReference<>(player));
            this.chunks.values().forEach(chunk -> chunk.visualize(player));
        }

        return this;
    }

    /**
     * Refresh all visualized blocks for all of the players
     * that have been previously {@link #visualize(Player)
     * visualized} to.
     * <p>
     * In other words, this method will {@link #reset() reset}
     * all blocks and then visualize again for all players.
     *
     * @return This AreaVisual object.
     */
    public AreaVisual refresh() {
        return this.refresh(null);
    }

    /**
     * Refresh all visualized blocks for all of the players
     * that have been previously {@link #visualize(Player)
     * visualized} to.
     * <p>
     * In other words, this method will {@link #reset() reset}
     * all blocks and then visualize again for all players.
     *
     * @param action The action to be run in between reset and re-visualization.
     *               This could be shifting, adding blocks etc.
     * @return This AreaVisual object.
     */
    public AreaVisual refresh(Runnable action) {

        if (!this.visualized.isEmpty()) {

            Set<Player> players = this.getPlayers();
            Map<Long, Set<VisualBlock>> snaps = new HashMap<>((int) (chunks.size() / 0.75));
            if (action != null) {
                this.chunks.forEach((hash, chunk) -> snaps.put(hash, chunk.snapshot()));
                action.run();
                this.chunks.forEach((hash, chunk) -> players.forEach(player ->
                        chunk.visualize(player, snaps.getOrDefault(hash, Collections.emptySet()))));
            } else {
                this.chunks.values().forEach(chunk -> players.forEach(chunk::visualize));
            }
        }

        // Cleanup chunks if there are no blocks
        this.chunks.values().removeIf(ChunkVisual::isEmpty);
        return this;
    }

    /**
     * Refresh a single {@link VisualBlock block} coordinate
     * for all of the visualized players. If the block is not
     * contained within this area visual, then this method will
     * do nothing.
     *
     * @param x The X coordinate of the block to refresh.
     * @param y The Y coordinate of the block to refresh.
     * @param z The Z coordinate of the block to refresh.
     * @return This AreaVisual object.
     */
    public AreaVisual refresh(int x, int y, int z) {
        ChunkVisual chunk = this.chunks.get(LongHash.toLong(x >> 4, z >> 4));
        return chunk != null ? this.forEach(player -> chunk.refresh(player, x, y, z)) : this;
    }

    /**
     * Reset all of the blocks that have been visualized within
     * this area.
     * <p>
     * If a {@link Player} that has been visualized to is not online
     * anymore, then the blocks will not be reset for them manually
     * and instead will reset on their own as soon as the player views
     * them again.
     *
     * @return This AreaVisual object.
     */
    public AreaVisual reset() {
        return this.forEach(player -> this.chunks.values().forEach(chunk -> chunk.reset(player)));
    }

    /**
     * Reset all of the blocks that have been visualized within
     * this area for the given {@link Player} if they have been
     * {@link #visualize(Player) visualized} to previously.
     *
     * @param player The player to reset the visualized blocks for.
     * @return This AreaVisual object.
     */
    public AreaVisual reset(Player player) {

        if (this.visualized.remove(player.getUniqueId()) != null) {
            this.chunks.values().forEach(chunk -> chunk.reset(player));
        }

        return this;
    }

    /**
     * Clear and reset all blocks that have been visualized within
     * this area.
     * <p>
     * If a {@link Player} that has been visualized to is not online
     * anymore, then the blocks will not be reset for them manually
     * and instead will reset on their own as soon as the player views
     * them again.
     *
     * @return This AreaVisual object.
     */
    public AreaVisual clear() {

        if (!this.visualized.isEmpty()) {

            synchronized (this.visualized) {
                this.forEach(player -> this.chunks.values().forEach(chunk -> chunk.reset(player)));
                this.chunks.values().forEach(ChunkVisual::clear);
                this.visualized.clear();
                this.chunks.clear();
            }
        }

        return this;
    }

    /**
     * Add a {@link VisualBlock block} to this area to be visualized.
     * If the block is already added to this area, then it will be replaced
     * with the new one at the same location.
     *
     * @param block The block to add.
     * @return This AreaVisual object.
     * @see VisualBlock
     */
    public AreaVisual addBlock(VisualBlock block) {
        this.chunks.computeIfAbsent(block.getChunk(), ChunkVisual::new).addBlock(block);
        return this;
    }

    /**
     * Add {@link VisualBlock blocks} to this area to be visualized.
     * If a block is already added to this area, then it will be replaced
     * with the new one at the same location.
     *
     * @param blocks The blocks to add.
     * @return This AreaVisual object.
     * @see VisualBlock
     */
    public AreaVisual addBlock(Collection<VisualBlock> blocks) {

        ChunkVisual chunk = null;
        for (VisualBlock block : blocks) {

            if (chunk == null || block.getChunk() != chunk.getChunk()) {
                chunk = this.chunks.computeIfAbsent(block.getChunk(), ChunkVisual::new);
            }

            chunk.addBlock(block);
        }

        return this;
    }

    /**
     * Remove the {@link VisualBlock block} at the given coordinates.
     * If the block coordinates are not contained within this area visual
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

        ChunkVisual chunk = this.chunks.get(LongHash.toLong(x >> 4, z >> 4));
        if (chunk == null) {
            return null;
        }

        VisualBlock block = chunk.removeBlock(x, y, z);
        if (block != null) {
            this.forEach(player -> chunk.sendBlocks(player,
                    Collections.singletonList(block.getRealType(player.getWorld()))));
        }

        return block;
    }

    /**
     * Get all of the blocks that are currently contained
     * within this area visual.
     *
     * @return A unique {@link Set} of blocks within this area.
     */
    public Set<VisualBlock> getBlocks() {
        Set<VisualBlock> blocks = new HashSet<>();
        this.chunks.values().forEach(chunk -> blocks.addAll(chunk.snapshot()));
        return blocks;
    }

    /**
     * Get the {@link VisualBlock block} at the given coordinates.
     * If the block coordinates are not contained within this area
     * visual or there is no block at the coordinates, then {@code null}
     * will be returned.
     *
     * @param x The X coordinate of the block to retrieve.
     * @param y The Y coordinate of the block to retrieve.
     * @param z The Z coordinate of the block to retrieve.
     * @return The VisualBlock at the given coordinates or {@code null}
     *         if there is no block at the coordinates.
     */
    public VisualBlock getBlock(int x, int y, int z) {
        ChunkVisual chunk = this.chunks.get(LongHash.toLong(x >> 4, z >> 4));
        return chunk != null ? chunk.getBlock(x, y, z) : null;
    }

    /**
     * Get all of the current {@link ChunkVisual chunks}
     * that are currently contained within this area visual.
     *
     * @return The chunks that belong to this area visual.
     */
    public Set<ChunkVisual> getChunks() {
        return new HashSet<>(this.chunks.values());
    }

    /**
     * Get the {@link ChunkVisual} that is for the chunk with
     * the given coordinates. If there is not a ChunkVisual for
     * the chunk with the coordinates contained in this area visual,
     * then {@code null} will be returned.
     *
     * @param x The X coordinate of the chunk to retrieve.
     * @param z The Z coordinate of the chunk to retrieve.
     * @return The ChunkVisual with the given coordinates that is
     *         contained in this area visual or {@code null} if there
     *         is no chunk with the coordinates.
     */
    public ChunkVisual getChunk(int x, int z) {
        return this.chunks.get(LongHash.toLong(x, z));
    }

    /**
     * Revert a change to this area visual that took place via one
     * of the {@code setType()} methods. This will completely destroy
     * the current block state of this area visual and replace it with
     * the last snapshot taken.
     * <p>
     * If there are no snapshots to revert to, then this method will
     * do nothing.
     * <p>
     * Currently, only the {@code setType()} methods support reverting
     * to snapshots. Transformations may be implemented in a future version.
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
     * @return This AreaVisual object after the revert.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public AreaVisual revert() {
        return this.revert(true);
    }

    /**
     * Revert a change to this area visual that took place via one
     * of the {@code setType()} methods. This will completely destroy
     * the current block state of this area visual and replace it with
     * the last snapshot taken.
     * <p>
     * If there are no snapshots to revert to, then this method will
     * do nothing.
     * <p>
     * Currently, only the {@code setType()} methods support reverting
     * to snapshots. Transformations may be implemented in a future version.
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
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the revert.
     * @return This AreaVisual object after the revert.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public AreaVisual revert(boolean refresh) {
        return this.revert(1, refresh);
    }

    /**
     * Revert a change to this area visual that took place via one of
     * the {@code setType()} methods. This will completely destroy the
     * current block state of this area visual and replace it with the
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
     * @return This AreaVisual object after the revert.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public AreaVisual revert(int amount) {
        return this.revert(amount, true);
    }

    /**
     * Revert a change to this area visual that took place via one of
     * the {@code setType()} methods. This will completely destroy the
     * current block state of this area visual and replace it with the
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
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the revert.
     * @return This AreaVisual object after the revert.
     * @see #setType(Material, Material)
     * @see #setType(Material, Material, int)
     * @see #setType(Material, int, Material)
     * @see #setType(Material, int, Material, int)
     */
    public AreaVisual revert(int amount, boolean refresh) {
        return this.alter(chunk -> chunk.revert(amount, this), refresh);
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, Material toType) {
        return this.setType(fromType, -1, toType, 0, true);
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the type change.
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, Material toType, boolean refresh) {
        return this.setType(fromType, -1, toType, 0, refresh);
    }

    /**
     * Change the {@link Material type} of all of the {@link VisualBlock blocks}
     * that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the type change.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @return This AreaVisual object after the types have been changed.
     * @see #revert()
     */
    public AreaVisual setType(Material fromType, Material toType,
                              boolean refresh, boolean ignoreDirectional) {
        return this.setType(fromType, -1, toType, 0, refresh, ignoreDirectional);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, Material toType, int toData) {
        return this.setType(fromType, -1, toType, toData, true);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the type change.
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, Material toType, int toData, boolean refresh) {
        return this.setType(fromType, -1, toType, toData, refresh);
    }

    /**
     * Change the {@link Material type} and data of all of the
     * {@link VisualBlock blocks} that match the type criteria set.
     *
     * @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the type change.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @return This AreaVisual object after the types have been changed.
     * @see #revert()
     */
    public AreaVisual setType(Material fromType, Material toType, int toData,
                              boolean refresh, boolean ignoreDirectional) {
        return this.setType(fromType, -1, toType, toData, refresh, ignoreDirectional);
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
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, int fromData, Material toType) {
        return this.setType(fromType, fromData, toType, 0, true);
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
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the type change.
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, int fromData, Material toType, boolean refresh) {
        return this.setType(fromType, fromData, toType, 0, refresh);
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
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the type change.
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @return This AreaVisual object after the types have been changed.
     * @see #revert()
     */
    public AreaVisual setType(Material fromType, int fromData,
                              Material toType, boolean refresh, boolean ignoreDirectional) {
        return this.setType(fromType, fromData, toType, 0, refresh, ignoreDirectional);
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
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, int fromData, Material toType, int toData) {
        return this.setType(fromType, fromData, toType, toData, true);
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
     *  @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param fromData The {@link VisualBlock#getData() data} of the
     *                 blocks to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     * @return This AreaVisual object after the types have been changed.
     */
    public AreaVisual setType(Material fromType, int fromData,
                              Material toType, int toData, boolean refresh) {
        return this.alter(chunk -> chunk.setType(fromType, fromData, toType, toData), refresh);
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
     *  @param fromType The {@link VisualBlock#getType() type} of blocks
     *                 to change to the new type.
     * @param fromData The {@link VisualBlock#getData() data} of the
     *                 blocks to change to the new type.
     * @param toType The type to change the matching blocks to.
     * @param toData The data to change the matching blocks to.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     * @param ignoreDirectional If {@link VisualBlock#isDirectional() directional}
     *                          blocks should be ignored and not change type.
     * @return This AreaVisual object after the types have been changed.
     * @see #revert()
     */
    public AreaVisual setType(Material fromType, int fromData,
                              Material toType, int toData, boolean refresh, boolean ignoreDirectional) {
        return this.alter(chunk -> chunk.setType(fromType, fromData, toType, toData, ignoreDirectional), refresh);
    }

    /**
     * Shift all of the blocks that are contained in this area visual
     * to be visualized a single block in a direction.
     *
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @return This AreaVisual object after it has been shifted.
     */
    public AreaVisual shift(BlockFace direction) {
        return this.transform(chunk -> chunk.shift(direction), true);
    }

    /**
     * Shift all of the blocks that are contained in this area visual
     * to be visualized a certain amount of blocks in a direction.
     * <p>
     * This method will automatically {@link #reset() reset} all blocks
     * that have been previously visualized and then re-visualize the
     * newly shifted blocks.
     *
     * @param distance The distance (in blocks) to shift.
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @return This AreaVisual object after it has been shifted.
     */
    public AreaVisual shift(int distance, BlockFace direction) {
        return this.transform(chunk -> chunk.shift(distance, direction), true);
    }

    /**
     * Shift all of the blocks that are contained in this area visual
     * to be visualized a certain amount of blocks along each of the
     * x, y and z axes.
     * <p>
     * This method will automatically {@link #reset() reset} all blocks
     * that have been previously visualized and then re-visualize the
     * newly shifted blocks.
     *
     * @param x The amount of blocks to shift along the x-axis.
     * @param y The amount of blocks to shift along the y-axis.
     * @param z The amount of blocks to shift along the z-axis.
     * @return This AreaVisual object after it has been shifted.
     */
    public AreaVisual shift(int x, int y, int z) {
        return this.transform(chunk -> chunk.shift(x, y, z), true);
    }

    /**
     * Rotate all of the blocks that are contained in this area visual to
     * be visualized the specified amount of degrees around the X-axis.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the X axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * west toward negative X.
     * <p>
     * This method will automatically {@link #reset() reset} all blocks
     * that have been previously visualized and then re-visualize the
     * newly shifted blocks.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return This AreaVisual object after it has been rotated.
     */
    public AreaVisual rotateX(Vector origin, float degrees) {
        return this.transform(chunk -> chunk.rotateX(origin, degrees), true);
    }

    /**
     * Rotate all of the blocks that are contained in this area visual to
     * be visualized the specified amount of degrees around the Y-axis.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the Y axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * down toward negative Y.
     * <p>
     * This method will automatically {@link #reset() reset} all blocks
     * that have been previously visualized and then re-visualize the
     * newly shifted blocks.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return This AreaVisual object after it has been rotated.
     */
    public AreaVisual rotateY(Vector origin, float degrees) {
        return this.transform(chunk -> chunk.rotateY(origin, degrees), true);
    }

    /**
     * Rotate all of the blocks that are contained in this area visual to
     * be visualized the specified amount of degrees around the Z-axis.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the Z axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * north toward negative Z.
     * <p>
     * This method will automatically {@link #reset() reset} all blocks
     * that have been previously visualized and then re-visualize the
     * newly shifted blocks.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return This AreaVisual object after it has been rotated.
     */
    public AreaVisual rotateZ(Vector origin, float degrees) {
        return this.transform(chunk -> chunk.rotateZ(origin, degrees), true);
    }

    /**
     * Transform this area visual by using the given {@link Function transformer}
     * which will execute a transformation.
     * <p>
     * The transformer will be called on each {@link ChunkVisual chunk} that is
     * contained within this area visual in order to transform then entire visual.
     *
     * @param transformer The function to use on each chunk to transform it.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the transformation.
     * @return This AreaVisual object after it has been transformed.
     */
    public AreaVisual transform(Function<ChunkVisual, List<VisualBlock>> transformer, boolean refresh) {

        if (this.chunks.isEmpty()) {
            return this;
        }

        if (refresh) {
            return this.refresh(() -> this.transform(transformer, false));
        }

        List<VisualBlock> overflow = new ArrayList<>();
        this.chunks.values().forEach(chunk -> overflow.addAll(transformer.apply(chunk)));
        if (!overflow.isEmpty()) {
            this.addBlock(overflow);
        }

        return this;
    }

    /**
     * Alter this area visual by using the {@link Consumer transformer}
     * which will execute a change on each of the chunks that does not
     * change their positions in any way; this is what separates this method
     * from {@link #transform(Function, boolean)}.
     * <p>
     * The transformer will be called on each {@link ChunkVisual chunk} that is
     * contained within this area visual in order to transform then entire visual.
     *
     * @param transformer The function to use on each chunk to transform it.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the transformation.
     * @return This AreaVisual object after it has been transformed.
     */
    // Named to alter to avoid lambda confusion
    private AreaVisual alter(Consumer<ChunkVisual> transformer, boolean refresh) {

        if (this.chunks.isEmpty()) {
            return this;
        }

        if (refresh) {
            return this.refresh(() -> this.alter(transformer, false));
        }

        this.chunks.values().forEach(transformer);
        return this;
    }
}
