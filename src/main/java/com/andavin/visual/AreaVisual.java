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

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * Visualize and send all of the fake blocks to the given
     * {@link Player client}. This is essentially an update method.
     * Any blocks that are added via {@link #addBlock(VisualBlock)}
     * or {@link #addBlock(List)} will not be sent to the client
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
    public AreaVisual visualize(final Player player) {

        synchronized (this.visualized) {
            this.visualized.computeIfAbsent(player.getUniqueId(), uuid -> new WeakReference<>(player));
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
    public AreaVisual refresh(final Runnable action) {

        if (!this.visualized.isEmpty()) {

            final Set<Player> players = this.visualized.values().stream().map(WeakReference::get)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            final Map<Long, Set<VisualBlock>> snaps = new HashMap<>((int) (chunks.size() / 0.75));
            if (action != null) {
                this.chunks.forEach((hash, chunk) -> snaps.put(hash, chunk.snapshot()));
                action.run();
                this.chunks.forEach((hash, chunk) -> players.forEach(player ->
                        chunk.visualize(player, snaps.getOrDefault(hash, Collections.emptySet()))));
            } else {
                this.chunks.values().forEach(chunk -> players.forEach(chunk::visualize));
            }
        }

        return this;
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

        if (!this.visualized.isEmpty()) {
            this.visualized.values().stream().map(WeakReference::get).filter(Objects::nonNull)
                    .forEach(player -> this.chunks.values().forEach(chunk -> chunk.reset(player)));
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
                this.visualized.values().stream().map(WeakReference::get).filter(Objects::nonNull)
                        .forEach(player -> this.chunks.values().forEach(chunk -> chunk.reset(player)));
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
    public AreaVisual addBlock(final VisualBlock block) {
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
    public AreaVisual addBlock(final List<VisualBlock> blocks) {

        ChunkVisual chunk = null;
        for (final VisualBlock block : blocks) {

            if (chunk == null || block.getChunk() != chunk.getChunk()) {
                chunk = this.chunks.computeIfAbsent(block.getChunk(), ChunkVisual::new);
            }

            chunk.addBlock(block);
        }

        return this;
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
    public AreaVisual setType(final Material fromType, final Material toType) {
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
    public AreaVisual setType(final Material fromType, final Material toType, final boolean refresh) {
        return this.setType(fromType, -1, toType, 0, refresh);
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
    public AreaVisual setType(final Material fromType, final Material toType, final int toData) {
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
    public AreaVisual setType(final Material fromType, final Material toType, final int toData, final boolean refresh) {
        return this.setType(fromType, -1, toType, toData, refresh);
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
    public AreaVisual setType(final Material fromType, final int fromData, final Material toType) {
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
    public AreaVisual setType(final Material fromType, final int fromData, final Material toType, final boolean refresh) {
        return this.setType(fromType, fromData, toType, 0, refresh);
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
    public AreaVisual setType(final Material fromType, final int fromData, final Material toType, final int toData) {
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
    public AreaVisual setType(final Material fromType, final int fromData,
            final Material toType, final int toData, final boolean refresh) {

        if (this.chunks.isEmpty()) {
            return this;
        }

        if (refresh) {
            this.refresh(() -> this.setType(fromType, fromData, toType, toData, false));
            return this;
        }

        this.chunks.values().forEach(chunk -> chunk.setType(fromType, fromData, toType, toData));
        return this;
    }

    /**
     * Shift all of the blocks that are contained in this area visual
     * to be visualized a single block in a direction.
     *
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @return This AreaVisual object after it has been shifted.
     */
    public AreaVisual shift(final BlockFace direction) {
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
    public AreaVisual shift(final int distance, final BlockFace direction) {
        return this.transform(chunk -> chunk.shift(distance, direction), true);
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
    public AreaVisual rotateX(final Vector origin, final float degrees) {
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
    public AreaVisual rotateY(final Vector origin, final float degrees) {
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
    public AreaVisual rotateZ(final Vector origin, final float degrees) {
        return this.transform(chunk -> chunk.rotateZ(origin, degrees), true);
    }

    /**
     * Transform this area visual by using the given {@link Function transformer}
     * which will execute a transformation.
     * <p>
     * The transformer will be called on each {@link ChunkVisual chunk} that is
     * contained within this area visual in order to transform then entire visual.
     *
     * @param transformer The function to use on each block to transform it.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the transformation.
     * @return This AreaVisual object after it has been transformed.
     */
    public AreaVisual transform(final Function<ChunkVisual, List<VisualBlock>> transformer, final boolean refresh) {

        if (this.chunks.isEmpty()) {
            return this;
        }

        if (refresh) {
            this.refresh(() -> this.transform(transformer, false));
            return this;
        }

        final List<VisualBlock> overflow = new ArrayList<>();
        this.chunks.values().forEach(chunk -> overflow.addAll(transformer.apply(chunk)));
        if (!overflow.isEmpty()) {
            this.addBlock(overflow);
        }

        // Cleanup chunks if there are no blocks
        this.chunks.values().removeIf(ChunkVisual::isEmpty);
        return this;
    }
}
