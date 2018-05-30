package com.andavin.visual;

import com.andavin.reflect.Reflection;
import com.andavin.util.LongHash;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

/**
 * A data holder for blocks that simply holds their
 * position and the type of block. This will not hold
 * world as it is relative to the player and everything
 * else can be calculated by the position and type.
 *
 * @since May 28, 2018
 * @author Andavin
 */
@SuppressWarnings("deprecation")
public final class VisualBlock {

    private static final List<Object> BLOCK_DATA;

    static {

        final Class<?> block = Reflection.getMcClass("Block");
        Object registry = Reflection.getValue(block, null, "REGISTRY_ID");
        if (registry == null) {
            registry = Reflection.getValue(block, null, "d");
        }

        Preconditions.checkState(registry != null, "incompatible with current server version");
        BLOCK_DATA = Reflection.getValue(registry.getClass(), registry, "b");
    }

    private final long chunk;
    private final int x, y, z;
    private final short packedPos;

    private final byte data;
    private final Material type;
    private final Object blockData;

    /**
     * Create a new visual block at the given position and of the basic
     * {@link Material type} with no data.
     *
     * @param x The X position of the block in the world.
     * @param y The Y position of the block in the world (0-255).
     * @param z The Z position of the block in the world.
     * @param type The {@link Material type} of the block that should be visualized.
     */
    public VisualBlock(final int x, final int y, final int z, final Material type) {
        this(x, y, z, type, 0);
    }

    /**
     * Create a new visual block at the given position and of the
     * {@link Material type} with data.
     *
     * @param x The X position of the block in the world.
     * @param y The Y position of the block in the world (0-255).
     * @param z The Z position of the block in the world.
     * @param type The {@link Material type} of the block that should be visualized.
     * @param data The data (0-15) that sets the block apart from others of its type;
     *             this is usually for color.
     */
    public VisualBlock(final int x, final int y, final int z, final Material type, final int data) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.data = (byte) data;
        this.chunk = LongHash.toLong(x >> 4, z >> 4);
        // Pack the position relative to the chunk 16x16x256
        // First 4 bits are X, the next 4 bits are Z, the last 8 bits are Y
        this.packedPos = (short) ((x & 0xF) << 12 | (z & 0xF) << 8 | y & 0xFF);
        // Pack the block type ID into a single number
        // First 12 bits are the type ID then last 4 bits are data 0-15
        final int packedType = (short) ((type.getId() & 0xFFF) << 4 | data & 0xF);
        Preconditions.checkState(0 <= packedType && packedType < BLOCK_DATA.size(),
                "%s:%s is not a valid block type with this server version", type, data);
        this.blockData = BLOCK_DATA.get(packedType);
    }

    /**
     * Get the X coordinate of the position
     * for this block in the world.
     *
     * @return The X coordinate of this block.
     */
    public int getX() {
        return x;
    }

    /**
     * Get the Y coordinate of the position
     * for this block in the world.
     *
     * @return The Y coordinate of this block.
     */
    public int getY() {
        return y;
    }

    /**
     * Get the Z coordinate of the position
     * for this block in the world.
     *
     * @return The Z coordinate of this block.
     */
    public int getZ() {
        return z;
    }

    /**
     * The {@link Material type} of this block.
     * This is the type of block without any data
     * attached.
     *
     * @return The type of block.
     */
    public Material getType() {
        return type;
    }

    /**
     * The data for this block (usually representing
     * something like color) that sets it apart from
     * other blocks of its type.
     * <p>
     * A good example of when this is used is different
     * colors of {@link Material#WOOL}.
     *
     * @return The data (0-15) for this block.
     */
    public byte getData() {
        return data;
    }

    /**
     * The X and Z coordinates of the chunk that contains
     * this block hashed into a single {@code long}.
     *
     * @return The chunk coordinate long.
     * @see LongHash
     */
    long getChunk() {
        return chunk;
    }

    /**
     * The position of this block within the {@link #getChunk() chunk}
     * packed into a single {@code short}.
     *
     * @return The packed position of this block.
     */
    short getPackedPos() {
        return packedPos;
    }

    /**
     * The {@code IBlockData} for this block. This is the
     * object that compacts the {@link Material type} and
     * the {@link #getData() data} into a single object that
     * the server and client interpret into a block type.
     *
     * @return The data for this block.
     */
    Object getBlockData() {
        return blockData;
    }

    /**
     * Get a VisualBlock for the block that is actually
     * in the world at the location of this block.
     *
     * @param world The world to get the type of the block from.
     * @return A new block of the type of the actual block at this block's location.
     */
    VisualBlock getRealType(final World world) {
        final Block block = world.getBlockAt(this.x, this.y, this.z);
        return new VisualBlock(this.x, this.y, this.z, block.getType(), block.getData());
    }

    /**
     * Shift this block a 1 block in a direction. The returned
     * object will be a new {@link VisualBlock} in the shifted location.
     *
     * @param direction The {@link BlockFace direction} to shift the block in.
     * @return A new block that is shifted relative to this block.
     */
    public VisualBlock shift(final BlockFace direction) {
        return this.shift(1, direction);
    }

    /**
     * Shift this block a certain amount in a direction.
     * The returned object will be a new {@link VisualBlock}
     * in the shifted location.
     *
     * @param distance The distance (in blocks) to shift in the direction.
     * @param direction The {@link BlockFace direction} to shift the block in.
     * @return A new block that is shifted relative to this block.
     */
    public VisualBlock shift(final int distance, final BlockFace direction) {
        return new VisualBlock(
                this.x + distance * direction.getModX(),
                this.y + distance * direction.getModY(),
                this.z + distance * direction.getModZ(),
                this.type,
                this.data
        );
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof VisualBlock)) {
            return false;
        }

        final VisualBlock block = (VisualBlock) obj;
        return block.chunk == this.chunk && block.packedPos == this.packedPos;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        return hash;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ") " + this.type + ':' + this.data;
    }
}
