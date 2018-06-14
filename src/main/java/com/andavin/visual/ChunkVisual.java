package com.andavin.visual;

import com.andavin.reflect.Reflection;
import com.andavin.util.LongHash;
import com.andavin.util.PacketSender;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @since May 28, 2018
 * @author Andavin
 */
public final class ChunkVisual {

    private static final int MAX_BLOCKS = 64;
    private static final Field CHUNK, M_BLOCK_DATA, POSITION, S_BLOCK_DATA;
    private static final Constructor<?> BLOCK_POS, M_PACKET, S_PACKET, MULTI_BLOCK, CHUNK_PAIR =
            Reflection.getConstructor(Reflection.getMcClass("ChunkCoordIntPair"), int.class, int.class);

    static {
        final Class<?> blockData = Reflection.getMcClass("IBlockData");
        final Class<?> singlePacket = Reflection.getMcClass("PacketPlayOutBlockChange");
        final Class<?> multiPacket = Reflection.getMcClass("PacketPlayOutMultiBlockChange");
        final Class<?> multiBlock = Reflection.getMcClass("PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
        BLOCK_POS = Reflection.getConstructor(Reflection.getMcClass("BlockPosition"), int.class, int.class, int.class);
        M_PACKET = Reflection.getConstructor(multiPacket);
        MULTI_BLOCK = Reflection.getConstructor(multiBlock, multiPacket, short.class, blockData);
        S_PACKET = Reflection.getConstructor(singlePacket);
        CHUNK = Reflection.getField(multiPacket, "a");
        M_BLOCK_DATA = Reflection.getField(multiPacket, "b");
        POSITION = Reflection.getField(singlePacket, "a");
        S_BLOCK_DATA = Reflection.getField(singlePacket, "block");
    }

    private final long chunk;
    private final Object chunkPair;
    private final Map<Short, VisualBlock> blocks = new HashMap<>();

    ChunkVisual(final long chunk) {
        this.chunk = chunk;
        //noinspection ConstantConditions
        this.chunkPair = Reflection.getInstance(CHUNK_PAIR, LongHash.msw(chunk), LongHash.lsw(chunk));
    }

    /**
     * Get the hashed {@code long} coordinates of this
     * visualized chunk.
     *
     * @return The {@code long} hash for this chunk.
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
        return this.blocks.isEmpty();
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

        if (!this.blocks.isEmpty()) {
            final List<Object> packets = new LinkedList<>();
            final List<VisualBlock> blocks = new ArrayList<>(this.blocks.values());
            this.createPackets(blocks, packets);
            PacketSender.sendPackets(player, packets);
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
         * 3. Blocks added
         * 2. Blocks removed
         */

        final World world = player.getWorld();
        final List<Object> packets = new LinkedList<>();
        final List<VisualBlock> needsUpdate = new LinkedList<>();
        snapshot.forEach(block -> {

            final VisualBlock current = this.blocks.get(block.getPackedPos());
            if (current == null) {
                // The block was removed
                needsUpdate.add(block.getRealType(world));
            } else if (current.getType() != block.getType() || current.getData() != block.getData()) {
                // The block type was changed
                needsUpdate.add(current);
            }
        });

        // Blocks that were added
        this.blocks.values().stream().filter(block -> !snapshot.contains(block)).forEach(needsUpdate::add);
        this.createPackets(needsUpdate, packets);
        if (!packets.isEmpty()) {
            PacketSender.sendPackets(player, packets);
        }
    }

    /**
     * Reset all of the {@link VisualBlock blocks} for the given
     * player to the actual blocks that are in the world.
     *
     * @param player The player to clear the blocks for.
     */
    public void reset(final Player player) {

        if (!this.blocks.isEmpty()) {
            final World world = player.getWorld();
            final List<Object> packets = new LinkedList<>();
            final List<VisualBlock> blocks = this.blocks.values().stream()
                    .map(block -> block.getRealType(world)).collect(Collectors.toList());
            this.createPackets(blocks, packets);
            PacketSender.sendPackets(player, packets);
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
    public String toString() {
        return this.chunkPair.toString();
    }

    private void createPackets(final List<VisualBlock> blocks, final List<Object> packets) {

        if (blocks.isEmpty()) {
            return;
        }

        // If there is only a single block to send then add
        // a single PacketPlayOutBlockChange packet
        if (blocks.size() == 1) {
            final VisualBlock block = blocks.get(0);
            final Object packet = Reflection.getInstance(S_PACKET);
            Reflection.setValue(POSITION, packet, Reflection.getInstance(BLOCK_POS,
                    block.getX(), block.getY(), block.getZ())); // Set the position
            Reflection.setValue(S_BLOCK_DATA, packet, block.getBlockData()); // And the data
            packets.add(packet); // add the packet
            return;
        }

        // There are multiple here so add a PacketPlayOutMultiBlockChange packet
        final Object packet = Reflection.getInstance(M_PACKET);
        final Object[] blockData = (Object[]) Array.newInstance(MULTI_BLOCK.getDeclaringClass(),
                Math.min(MAX_BLOCKS, blocks.size())); // Up to MAX_BLOCKS blocks at a time
        Reflection.setValue(CHUNK, packet, this.chunkPair); // Set the chunk that it is in
        Reflection.setValue(M_BLOCK_DATA, packet, blockData); // Place the array into the packet

        // Then update the array with all of the data and positions
        int i = 0;
        final Iterator<VisualBlock> itr = blocks.iterator();
        while (itr.hasNext()) {

            final VisualBlock block = itr.next();
            blockData[i++] = Reflection.getInstance(MULTI_BLOCK, packet, block.getPackedPos(), block.getBlockData());
            itr.remove(); // Remove every one we add
            if (i >= MAX_BLOCKS) {
                // If we've added MAX_BLOCKS then start a new packet for the rest
                this.createPackets(blocks, packets);
                break;
            }
        }

        // Add the packet to the packets to send
        packets.add(packet);
    }
}
