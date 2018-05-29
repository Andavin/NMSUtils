package com.andavin.visual;

import com.andavin.reflect.Reflection;
import com.andavin.util.LongHash;
import com.andavin.util.PacketSender;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @since May 28, 2018
 * @author Andavin
 */
@SuppressWarnings("ConstantConditions")
final class ChunkVisual {

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
    private final Set<VisualBlock> blocks = new HashSet<>();

    ChunkVisual(final long chunk) {
        this.chunk = chunk;
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
     * Add a new block to this chunk. If the block is already
     * present in this chunk then it will be replaced and sent
     * to the clients on the next update.
     *
     * @param block The {@link VisualBlock block} to add.
     */
    public void addBlock(final VisualBlock block) {
        this.blocks.add(block);
    }

    /**
     * Send all of the fake block types to the given {@link Player}.
     *
     * @param player The player to send the {@link VisualBlock blocks} to.
     */
    public void visualize(final Player player) {

        if (!this.blocks.isEmpty()) {
            final List<Object> packets = new LinkedList<>();
            final List<VisualBlock> blocks = new ArrayList<>(this.blocks);
            this.createPackets(blocks, packets);
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
            final List<VisualBlock> blocks = this.blocks.stream()
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
