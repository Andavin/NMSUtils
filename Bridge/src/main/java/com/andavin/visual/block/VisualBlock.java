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

package com.andavin.visual.block;

import com.andavin.Versioned;
import com.andavin.util.LocationUtil;
import com.andavin.util.LongHash;
import com.andavin.util.MinecraftVersion;
import com.andavin.visual.VisualBridge;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.*;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicLong;

import static com.andavin.util.MinecraftVersion.v1_12;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * A data holder for blocks that simply holds their position
 * and the type of block. This will not hold world as it is
 * relative to the player.
 * <p>
 * This object is immutable. Any methods that alter this object
 * will simply return a new object:
 * <ul>
 *     <li>{@link #shift(BlockFace)}</li>
 *     <li>{@link #shift(int, int, int)}</li>
 *     <li>{@link #shift(int, BlockFace)}</li>
 *     <li>{@link #rotateX(Vector, float)}</li>
 *     <li>{@link #rotateY(Vector, float)}</li>
 *     <li>{@link #rotateZ(Vector, float)}</li>
 * </ul>
 * The above methods give the impression of altering this object,
 * however, they will instead return a new object with new values,
 * but will carry the {@link #getId() ID} of the original object over.
 * <p>
 * Therefore, this object is fully thread-safe and can be passed
 * around over threads or anywhere without fear of alteration.
 *
 * @since May 28, 2018
 * @author Andavin
 */
public final class VisualBlock {

    private static final AtomicLong ID = new AtomicLong();
    private static final VisualBridge BRIDGE = Versioned.getInstance(VisualBridge.class);

    private final long id;
    private final long chunk;
    private final int x, y, z;
    private final short packedPos;

    private final byte data;
    private final Material type;
    private final boolean directional;
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
    public VisualBlock(int x, int y, int z, Material type) {
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
    public VisualBlock(int x, int y, int z, Material type, int data) {
        this(ID.getAndIncrement(), x, y, z, type, data,
                MinecraftVersion.greaterThan(v1_12) ? type.createBlockData() : null);
    }

    /**
     * Create a new visual block at the given position and of the
     * {@link Material type} with data and also giving a snapshot
     * of the previous state of this block.
     * <p>
     * This constructor is only used by internal transformation methods
     * and reversion methods; therefore access is restricted.
     *
     * @param id The unique ID of this block.
     * @param x The X position of the block in the world.
     * @param y The Y position of the block in the world (0-255).
     * @param z The Z position of the block in the world.
     * @param type The {@link Material type} of the block that should be visualized.
     * @param data The data (0-15) that sets the block apart from others of its type;
     *             this is usually for color.
     */
    public VisualBlock(long id, int x, int y, int z, Material type, int data, Object blockData) {
        checkArgument(type.isBlock(), type + " is not a block");
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.data = (byte) data;
        this.blockData = blockData;
        this.chunk = LongHash.toLong(x >> 4, z >> 4);
        // Pack the position relative to the chunk 16x16x256
        // First 4 bits are X, the next 4 bits are Z, the last 8 bits are Y
        this.packedPos = (short) ((x & 0xF) << 12 | (z & 0xF) << 8 | y & 0xFF);
        this.directional = BRIDGE.isDirectional(type);
    }

    /**
     * Get the ID of this block. This is a unique ID for this block
     * and any blocks that are transformed from this block. Only
     * transformation methods will maintain this ID on the new block:
     * <ul>
     *     <li>{@link #shift(BlockFace)}</li>
     *     <li>{@link #shift(int, int, int)}</li>
     *     <li>{@link #shift(int, BlockFace)}</li>
     *     <li>{@link #rotateX(Vector, float)}</li>
     *     <li>{@link #rotateY(Vector, float)}</li>
     *     <li>{@link #rotateZ(Vector, float)}</li>
     * </ul>
     * Note that this ID is only unique for each session of the JVM.
     * Each time the static initializer of this class is executed
     * the IDs still reset to {@code 0}.
     * <p>
     * The value returned by this method is not meant to be a unique
     * ID in any kind of database as it will <i>not</i> maintain its
     * uniqueness.
     * <p>
     * This is mostly meant for internal use by {@code ChunkVisual},
     * but is made {@code public} for any case where an ID is needed
     * to be maintained over transformations.
     *
     * @return The unique ID of this block and its transformations.
     */
    public long getId() {
        return id;
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
     * colors of wool.
     * <p>
     * Note that, in Minecraft 1.13+, this value has no
     * effect on the block since data was done away with.
     *
     * @return The data (0-15) for this block.
     */
    public byte getData() {
        return data;
    }

    /**
     * Tell if this VisualBlock {@link #getType() type} is
     * a directional block type. More specifically if the
     * {@link Material#getData() type data} extends any of
     * the following directional classes:
     * <ul>
     *     <li>{@link Directional}</li>
     *     <li>{@link Tree}</li>
     *     <li>{@link Rails}</li>
     *     <li>{@link ExtendedRails}</li>
     * </ul>
     * The Rails and ExtendedRails classes will only be rotated
     * from within this object (using the rotation methods) under
     * specific circumstances:
     * <ol>
     *     <li>Only ever from {@link #rotateY(Vector, float)}</li>
     *     <li>ExtendedRails must be rotated in increments of 90º</li>
     * </ol>
     *
     * @return If this block is a directional {@link Material type}.
     */
    public boolean isDirectional() {
        return directional;
    }

    /**
     * The X and Z coordinates of the chunk that contains
     * this block hashed into a single {@code long}.
     *
     * @return The chunk coordinate long.
     * @see LongHash
     */
    public long getChunk() {
        return chunk;
    }

    /**
     * The position of this block within the {@link #getChunk() chunk}
     * packed into a single {@code short}.
     *
     * @return The packed position of this block.
     */
    public short getPackedPosition() {
        return packedPos;
    }

    /**
     * The {@code BlockData} for this block. This is the
     * Minecraft 1.13+ object that specifies all details
     * about the type of the block. If the server is an
     * earlier version than 1.13, then this will be {@code null}.
     *
     * @return The data for this block.
     */
    public Object getBlockData() {
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
    public VisualBlock getRealType(World world) {
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
    public VisualBlock getRealType(Chunk chunk) {
        Block block = chunk.getBlock(this.x & 0xF, this.y & 0xFF, this.z & 0xF);
        return new VisualBlock(this.x, this.y, this.z, block.getType(), block.getData());
    }

    /**
     * Get a VisualBlock for the block that is actually in the
     * {@link ChunkSnapshot} at the location of this block.
     *
     * @param snapshot A snapshot of the chunk that this block is contained within.
     * @return A new block of the type of the actual block at this block's location.
     */
    public VisualBlock getRealType(ChunkSnapshot snapshot) {
        int x = this.x & 0xF, y = this.y & 0xFF, z = this.z & 0xF;
        return BRIDGE.getRealType(this, snapshot, x, y, z);
    }

    /**
     * Shift this block a 1 block in a direction. The returned
     * object will be a new {@link VisualBlock} in the shifted location.
     *
     * @param direction The {@link BlockFace direction} to shift the block in.
     * @return A new block that is shifted relative to this block.
     */
    public VisualBlock shift(BlockFace direction) {
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
    public VisualBlock shift(int distance, BlockFace direction) {
        return new VisualBlock(
                this.id,
                this.x + distance * direction.getModX(),
                this.y + distance * direction.getModY(),
                this.z + distance * direction.getModZ(),
                this.type,
                this.data,
                this.blockData
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
    public VisualBlock shift(int x, int y, int z) {
        return new VisualBlock(
                this.id,
                this.x + x,
                this.y + y,
                this.z + z,
                this.type,
                this.data,
                this.blockData
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
    public VisualBlock rotateX(Vector origin, float degrees) {

        degrees %= 360;
        byte data;
        if (this.directional) {

            Class<? extends MaterialData> dataType = this.type.getData();
            if (Door.class.isAssignableFrom(dataType) && (this.data & 0x8) == 0x8) {
                data = this.data;
            } else if (Directional.class.isAssignableFrom(dataType)) {
                MaterialData typeData = this.type.getNewData(this.data);
                Directional direction = (Directional) typeData;
                BlockFace face = LocationUtil.rotate(direction.getFacing(), degrees, true, false);
                direction.setFacingDirection(face);
                data = typeData.getData();
            } else if (Tree.class.isAssignableFrom(dataType)) {
                Tree tree = (Tree) this.type.getNewData(this.data);
                BlockFace face = LocationUtil.rotate(tree.getDirection(), degrees, true, false);
                tree.setDirection(face);
                data = tree.getData();
            } else {
                data = this.data;
            }
        } else {
            data = this.data;
        }

        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();
        double cos = dCos(degrees), sin = -dSin(degrees);
        return new VisualBlock(
                this.id,
                this.x,
                (int) (originY + (this.y - originY) * cos - (this.z - originZ) * sin),
                (int) (originZ + (this.z - originZ) * cos + (this.y - originY) * sin),
                this.type,
                data,
                this.blockData
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
    public VisualBlock rotateY(Vector origin, float degrees) {

        degrees %= 360;
        byte data;
        if (this.directional) {

            Class<? extends MaterialData> dataType = this.type.getData();
            if (Door.class.isAssignableFrom(dataType) && (this.data & 0x8) == 0x8) { // Top half of the door (not directional)
                data = this.data;
            } else if (Directional.class.isAssignableFrom(dataType)) {
                MaterialData typeData = this.type.getNewData(this.data);
                Directional direction = (Directional) typeData;
                BlockFace face = LocationUtil.rotate(direction.getFacing(), degrees, false, false);
                direction.setFacingDirection(face);
                data = typeData.getData();
            } else if (Tree.class.isAssignableFrom(dataType)) {
                Tree tree = (Tree) this.type.getNewData(this.data);
                BlockFace face = LocationUtil.rotate(tree.getDirection(), degrees, false, false);
                tree.setDirection(face);
                data = tree.getData();
            } else if (Rails.class.isAssignableFrom(dataType) &&
                    (degrees % 90 == 0 || !ExtendedRails.class.isAssignableFrom(dataType))) {
                Rails rails = (Rails) this.type.getNewData(this.data);
                BlockFace face = LocationUtil.rotate(rails.getDirection(), degrees, false, false);
                rails.setDirection(face, rails.isOnSlope());
                data = rails.getData();
            } else {

                if (this.type == Material.ANVIL) {
                    BlockFace face = LocationUtil.rotate(this.getAnvilDirection(), degrees, false, false);
                    data = this.getDataForDirection(face);
                } else {
                    data = this.data;
                }
            }
        } else {
            data = this.data;
        }

        int originX = origin.getBlockX();
        int originZ = origin.getBlockZ();
        double cos = dCos(degrees), sin = -dSin(degrees);
        return new VisualBlock(
                this.id,
                (int) (originX + (this.x - originX) * cos + (this.z - originZ) * sin),
                this.y,
                (int) (originZ + (this.z - originZ) * cos - (this.x - originX) * sin),
                this.type,
                data,
                this.blockData
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
    public VisualBlock rotateZ(Vector origin, float degrees) {

        degrees %= 360;
        byte data;
        if (this.directional) {

            Class<? extends MaterialData> dataType = this.type.getData();
            if (Door.class.isAssignableFrom(dataType) && (this.data & 0x8) == 0x8) {
                data = this.data;
            } else if (Directional.class.isAssignableFrom(dataType)) {
                MaterialData typeData = this.type.getNewData(this.data);
                Directional direction = (Directional) typeData;
                BlockFace face = LocationUtil.rotate(direction.getFacing(), degrees, false, true);
                direction.setFacingDirection(face);
                data = typeData.getData();
            } else if (Tree.class.isAssignableFrom(dataType)) {
                Tree tree = (Tree) this.type.getNewData(this.data);
                BlockFace face = LocationUtil.rotate(tree.getDirection(), degrees, false, true);
                tree.setDirection(face);
                data = tree.getData();
            } else {
                data = this.data;
            }
        } else {
            data = this.data;
        }

        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        double cos = dCos(degrees), sin = -dSin(degrees);
        return new VisualBlock(
                this.id,
                (int) (originX + (this.x - originX) * cos - (this.y - originY) * sin),
                (int) (originY + (this.y - originY) * cos + (this.x - originX) * sin),
                this.z,
                this.type,
                data,
                this.blockData
        );
    }

    /**
     * Create a {@link BlockVector Vector} using this block's
     * location coordinates {@link #getX() X}, {@link #getY() Y}
     * and {@link #getZ() Z}.
     *
     * @return This block's location as a Vector.
     */
    public BlockVector toVector() {
        return new BlockVector(this.x, this.y, this.z);
    }

    /**
     * Check whether this and another VisualBlock are equal
     * to each other in position, {@link #getType() type}
     * and {@link #getData() data}. That is:
     * <pre>    this.x == other.x &amp;&amp; this.y == other.y &amp;&amp; this.z == other.z
     *  &amp;&amp; this.type == other.type &amp;&amp; this.data == other.data</pre>
     * If anything less than this needs to be checked it can be
     * checked individually using the provided getter method:
     * <ul>
     *     <li>{@link #getX()}</li>
     *     <li>{@link #getY()}</li>
     *     <li>{@link #getZ()}</li>
     *     <li>{@link #getType()}</li>
     *     <li>{@link #getData()}</li>
     * </ul>
     * Note that this method does not include an {@link #getId() ID}
     * check as the ID is a carry for transformation and is not
     * involved in position and type checks.
     *
     * @param other The other VisualBlock to check against this one.
     * @return If the two blocks are equivalent in position, type and data.
     */
    public boolean equals(VisualBlock other) {
        return this.chunk == other.chunk && this.packedPos == other.packedPos
                && this.type == other.type && this.data == other.data;
    }

    /**
     * Check whether this and another VisualBlock are at the
     * same position by coordinates. That is:
     * <pre>    this.x == obj.x &amp;&amp; this.y == obj.y &amp;&amp; this.z == obj.z</pre>
     * This method will <i>not</i> check whether the {@link #getType() type}
     * or {@link #getData() data} of this block is the same as another.
     * <p>
     * In order to check type the {@link #equals(VisualBlock)} can be
     * used which will check position and type.
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof VisualBlock)) {
            return false;
        }

        VisualBlock block = (VisualBlock) obj;
        return block.chunk == this.chunk && block.packedPos == this.packedPos;
    }

    @Override
    public int hashCode() {
        return this.x >> 13 ^ this.y >> 7 ^ this.z;
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ") " + this.type + ':' + this.data;
    }

    private BlockFace getAnvilDirection() {

        switch (this.data) {
            case 0:
            case 4:
            case 8:
                return BlockFace.NORTH;
            case 1:
            case 5:
            case 9:
                return BlockFace.EAST;
            case 2:
            case 6:
            case 10:
                return BlockFace.SOUTH;
            case 3:
            case 7:
            case 11:
                return BlockFace.WEST;
            default:
                return BlockFace.SELF;
        }
    }

    private byte getDataForDirection(BlockFace face) {

        switch (face) {
            case NORTH:
                return (byte) (this.data < 4 ? 0 : this.data < 8 ? 4 : 8);
            case EAST:
                return (byte) (this.data < 4 ? 1 : this.data < 8 ? 5 : 9);
            case SOUTH:
                return (byte) (this.data < 4 ? 2 : this.data < 8 ? 6 : 10);
            case WEST:
                return (byte) (this.data < 4 ? 3 : this.data < 8 ? 7 : 11);
            default:
                return this.data;
        }
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
    private static double dCos(double degrees) {

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
    private static double dSin(double degrees) {

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
