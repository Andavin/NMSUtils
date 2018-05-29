package com.andavin.visual;

import org.bukkit.entity.Player;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
}
