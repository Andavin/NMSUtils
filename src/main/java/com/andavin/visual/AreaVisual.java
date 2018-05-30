package com.andavin.visual;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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

    private final Map<Long, ChunkVisual> chunks = new HashMap<>();
    private final Map<UUID, WeakReference<Player>> visualized = new HashMap<>();

    /**
     * Visualize and send all of the fake blocks to the given
     * {@link Player client}. This is essentially an update method.
     * Any blocks that are added via {@link #addBlock(VisualBlock...)}
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
     */
    public void visualize(final Player player) {
        this.visualized.computeIfAbsent(player.getUniqueId(), uuid -> new WeakReference<>(player));
        this.chunks.values().forEach(chunk -> chunk.visualize(player));
    }

    /**
     * Refresh all visualized blocks for all of the players
     * that have been previously {@link #visualize(Player)
     * visualized} to.
     * <p>
     * In other words, this method will {@link #reset() reset}
     * all blocks and then visualize again for all players.
     */
    public void refresh() {
        this.refresh(null);
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
     */
    public void refresh(final Runnable action) {

        // TODO a refresh that optimizing removing and recreating
        // Take a snapshot of the chunk's blocks
        // Take action on the chunk
        // Reset blocks that were removed while adding new blocks
        if (!this.visualized.isEmpty()) {

            this.visualized.values().stream().map(WeakReference::get).filter(Objects::nonNull)
                    .forEach(player -> this.chunks.values().forEach(chunk -> {

                        if (action != null) {
                            final Set<VisualBlock> blocks = chunk.snapshot();
                            action.run();
                            chunk.visualize(player, blocks);
                        } else {
                            chunk.visualize(player, chunk.snapshot());
                        }
                    }));
        }
    }

    /**
     * Reset all of the blocks that have been visualized within
     * this area.
     * <p>
     * If a {@link Player} that has been visualized to is not online
     * anymore, then the blocks will not be reset for them manually
     * and instead will reset on their own as soon as the player views
     * them again.
     */
    public void reset() {

        if (!this.visualized.isEmpty()) {
            this.visualized.values().stream().map(WeakReference::get).filter(Objects::nonNull)
                    .forEach(player -> this.chunks.values().forEach(chunk -> chunk.reset(player)));
        }
    }

    /**
     * Clear and reset all blocks that have been visualized within
     * this area.
     * <p>
     * If a {@link Player} that has been visualized to is not online
     * anymore, then the blocks will not be reset for them manually
     * and instead will reset on their own as soon as the player views
     * them again.
     */
    public void clear() {

        if (!this.visualized.isEmpty()) {
            this.visualized.values().stream().map(WeakReference::get).filter(Objects::nonNull)
                    .forEach(player -> this.chunks.values().forEach(chunk -> chunk.reset(player)));
            this.chunks.values().forEach(ChunkVisual::clear);
            this.visualized.clear();
            this.chunks.clear();
        }
    }

    /**
     * Add {@link VisualBlock blocks} to this area to be visualized.
     * If a block is already added to this area, then it will be replaced
     * with the new one at the same location.
     *
     * @param blocks The blocks to add.
     * @see VisualBlock
     */
    public void addBlock(final VisualBlock... blocks) {
        this.addBlock(Arrays.asList(blocks));
    }

    /**
     * Add {@link VisualBlock blocks} to this area to be visualized.
     * If a block is already added to this area, then it will be replaced
     * with the new one at the same location.
     *
     * @param blocks The blocks to add.
     * @see VisualBlock
     */
    public void addBlock(final List<VisualBlock> blocks) {

        ChunkVisual chunk = null;
        for (final VisualBlock block : blocks) {

            if (chunk == null || block.getChunk() != chunk.getChunk()) {
                chunk = this.chunks.computeIfAbsent(block.getChunk(), ChunkVisual::new);
            }

            chunk.addBlock(block);
        }
    }

    /**
     * Shift all of the blocks that are contained in this area visual
     * to be visualized a single block in a direction.
     *
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @return This AreaVisual object after it has been shifted.
     */
    public AreaVisual shift(final BlockFace direction) {
        return this.shift(1, direction);
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
     * @return The leftover blocks that are no longer in this chunk.
     */
    public AreaVisual shift(final int distance, final BlockFace direction) {
        return this.shift(distance, direction, true);
    }

    /**
     * Shift all of the blocks that are contained in this area visual
     * to be visualized a certain amount of blocks in a direction.
     *
     * @param distance The distance (in blocks) to shift.
     * @param direction The {@link BlockFace direction} to shift the blocks in.
     * @param refresh If the blocks should be {@link #refresh(Runnable) refreshed}
     *                automatically during the shift.
     * @return The leftover blocks that are no longer in this chunk.
     */
    public AreaVisual shift(final int distance, final BlockFace direction, final boolean refresh) {

        if (this.chunks.isEmpty()) {
            return this;
        }

        if (refresh) {
            this.refresh(() -> this.shift(distance, direction, false));
            return this;
        }

        final List<VisualBlock> overflow = new LinkedList<>();
        this.chunks.values().forEach(chunk -> overflow.addAll(chunk.shift(distance, direction)));
        if (!overflow.isEmpty()) {
            this.addBlock(overflow);
        }

        return this;
    }
}
