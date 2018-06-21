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
import com.andavin.util.LocationUtil;
import com.andavin.util.LongHash;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

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

        checkState(registry != null, "incompatible with current server version");
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
        checkState(0 <= packedType && packedType < BLOCK_DATA.size(),
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
     * in the {@link World} at the location of this block.
     * <p>
     * Note that if the chunk that contains this block is
     * not loaded at the time of this method call, then this
     * method will force it to load synchronously.
     *
     * @param world The world to get the type of the block from.
     * @return A new block of the type of the actual block at this block's location.
     */
    public VisualBlock getRealType(final World world) {
        return this.getRealType(world.getChunkAt(this.x >> 4, this.z >> 4));
    }

    /**
     * Get a VisualBlock for the block that is actually
     * in the {@link Chunk} at the location of this block.
     * <p>
     * Note that if the given chunk is not loaded, then this
     * method will force it to load synchronously.
     *
     * @param chunk The chunk to get the block from within.
     * @return A new block of the type of the actual block at this block's location.
     */
    public VisualBlock getRealType(final Chunk chunk) {
        final Block block = chunk.getBlock(this.x & 0xF, this.y & 0xFF, this.z & 0xF);
        return new VisualBlock(this.x, this.y, this.z, block.getType(), block.getData());
    }

    /**
     * Get a VisualBlock for the block that is actually in the
     * {@link ChunkSnapshot} at the location of this block.
     *
     * @param snapshot A snapshot of the chunk that this block is contained within.
     * @return A new block of the type of the actual block at this block's location.
     */
    public VisualBlock getRealType(final ChunkSnapshot snapshot) {
        final int x = this.x & 0xF, y = this.y & 0xFF, z = this.z & 0xF;
        final Material type = Material.getMaterial(snapshot.getBlockTypeId(x, y, z));
        return new VisualBlock(this.x, this.y, this.z, type, snapshot.getBlockData(x, y, z));
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

    /**
     * Shift the block a certain amount along the given axes.
     * This works the same as an {@code add(int, int, int)}
     * method. The returned object will be a new {@link VisualBlock}
     * in the shifted location.
     *
     * @param x The amount of blocks to shift along the x-axis.
     * @param y The amount of blocks to shift along the y-axis.
     * @param z The amount of blocks to shift along the z-axis.
     * @return A new block that is shifted relative to this block.
     */
    public VisualBlock shift(final int x, final int y, final int z) {
        return new VisualBlock(
                this.x + x,
                this.y + y,
                this.z + z,
                this.type,
                this.data
        );
    }

    /**
     * Rotate this block the specified amount of degrees around
     * the X-axis. The returned object will be a new {@link VisualBlock}
     * in the new rotated location.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the X axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * west toward negative X.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return A new block that is rotated the specified degrees around
     *         the X-axis based on the origin.
     */
    public VisualBlock rotateX(final Vector origin, float degrees) {

        degrees %= 360;
        final byte data;
        final Class<? extends MaterialData> dataType = this.type.getData();
        if (dataType.isAssignableFrom(Directional.class)) {
            final MaterialData typeData = this.type.getNewData(this.data);
            final Directional direction = (Directional) this.type.getNewData(this.data);
            final BlockFace face = LocationUtil.rotate(direction.getFacing(), degrees, true, false);
            direction.setFacingDirection(face);
            data = typeData.getData();
        } else {
            data = this.data;
        }

        final int originY = origin.getBlockY();
        final int originZ = origin.getBlockZ();
        final double cos = dCos(degrees), sin = -dSin(degrees);
        return new VisualBlock(
                this.x,
                (int) (originY + (this.y - originY) * cos - (this.z - originZ) * sin),
                (int) (originZ + (this.z - originZ) * cos + (this.y - originY) * sin),
                this.type,
                data
        );
    }

    /**
     * Rotate this block the specified amount of degrees around
     * the Y-axis. The returned object will be a new {@link VisualBlock}
     * in the new rotated location.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the Y axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * down toward negative Y.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return A new block that is rotated the specified degrees around
     *         the Y-axis based on the origin.
     */
    public VisualBlock rotateY(final Vector origin, float degrees) {

        degrees %= 360;
        final byte data;
        final Class<? extends MaterialData> dataType = this.type.getData();
        if (dataType.isAssignableFrom(Directional.class)) {
            final MaterialData typeData = this.type.getNewData(this.data);
            final Directional direction = (Directional) this.type.getNewData(this.data);
            final BlockFace face = LocationUtil.rotate(direction.getFacing(), degrees, false, false);
            direction.setFacingDirection(face);
            data = typeData.getData();
        } else {
            data = this.data;
        }

        final int originX = origin.getBlockX();
        final int originZ = origin.getBlockZ();
        final double cos = dCos(degrees), sin = -dSin(degrees);
        return new VisualBlock(
                (int) (originX + (this.x - originX) * cos + (this.z - originZ) * sin),
                this.y,
                (int) (originZ + (this.z - originZ) * cos - (this.x - originX) * sin),
                this.type,
                this.data
        );
    }

    /**
     * Rotate this block the specified amount of degrees around
     * the Z-axis. The returned object will be a new {@link VisualBlock}
     * in the new rotated location.
     * <p>
     * In this case, positive degrees will result in a clockwise
     * rotation around the Z axis and inversely a negative will
     * result in a counterclockwise rotation if you are looking
     * north toward negative Z.
     *
     * @param origin The {@link Vector origin} to rotate around.
     * @param degrees The amount of degrees to rotate around the origin.
     * @return A new block that is rotated the specified degrees around
     *         the Z-axis based on the origin.
     */
    public VisualBlock rotateZ(final Vector origin, float degrees) {

        degrees %= 360;
        final byte data;
        final Class<? extends MaterialData> dataType = this.type.getData();
        if (dataType.isAssignableFrom(Directional.class)) {
            final MaterialData typeData = this.type.getNewData(this.data);
            final Directional direction = (Directional) this.type.getNewData(this.data);
            final BlockFace face = LocationUtil.rotate(direction.getFacing(), degrees, false, true);
            direction.setFacingDirection(face);
            data = typeData.getData();
        } else {
            data = this.data;
        }

        final int originX = origin.getBlockX();
        final int originY = origin.getBlockY();
        final double cos = dCos(degrees), sin = -dSin(degrees);
        return new VisualBlock(
                (int) (originX + (this.x - originX) * cos - (this.y - originY) * sin),
                (int) (originY + (this.y - originY) * cos + (this.x - originX) * sin),
                this.z,
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

    /**
     * Returns the cosine of an angle given in degrees. This is better than
     * just {@code Math.cos(Math.toRadians(degrees))} because it provides a
     * more accurate result for angles divisible by 90 degrees.
     * <p>
     * Taken from WorldEdit <a href="https://bit.ly/2y6lF3X">MathUtils class</a>
     *
     * @param degrees the angle
     * @return the cosine of the given angle
     */
    private static double dCos(final double degrees) {

        int dInt = (int) degrees;
        if (degrees == dInt && dInt % 90 == 0) {

            dInt %= 360;
            if (dInt < 0) {
                dInt += 360;
            }

            switch (dInt) {
                case 0:
                    return 1.0;
                case 90:
                    return 0.0;
                case 180:
                    return -1.0;
                case 270:
                    return 0.0;
            }
        }

        return Math.cos(Math.toRadians(degrees));
    }

    /**
     * Returns the sine of an angle given in degrees. This is better than just
     * {@code Math.sin(Math.toRadians(degrees))} because it provides a more
     * accurate result for angles divisible by 90 degrees.
     * <p>
     * Taken from WorldEdit <a href="https://bit.ly/2y6lF3X">MathUtils class</a>
     *
     * @param degrees the angle
     * @return the sine of the given angle
     */
    private static double dSin(final double degrees) {

        int dInt = (int) degrees;
        if (degrees == dInt && dInt % 90 == 0) {

            dInt %= 360;
            if (dInt < 0) {
                dInt += 360;
            }

            switch (dInt) {
                case 0:
                    return 0.0;
                case 90:
                    return 1.0;
                case 180:
                    return 0.0;
                case 270:
                    return -1.0;
            }
        }

        return Math.sin(Math.toRadians(degrees));
    }
}
