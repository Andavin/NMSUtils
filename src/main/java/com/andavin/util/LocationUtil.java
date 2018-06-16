package com.andavin.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public final class LocationUtil {

    private static final BlockFace[] CARDINAL = {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };

    private static final BlockFace[] DIAGONAL = {
            BlockFace.NORTH, BlockFace.NORTH_EAST,
            BlockFace.EAST, BlockFace.SOUTH_EAST,
            BlockFace.SOUTH, BlockFace.SOUTH_WEST,
            BlockFace.WEST, BlockFace.NORTH_WEST
    };

    /**
     * Get a copy of the given location that is at the center
     * of the block that the location is on.
     *
     * @param loc The location to get the block location from.
     * @return The centered location.
     */
    public static Location center(final Location loc) {
        return new Location(loc.getWorld(),
                loc.getBlockX() + 0.5,
                loc.getBlockY() + 0.5,
                loc.getBlockZ() + 0.5,
                loc.getYaw(),
                loc.getPitch());
    }

    /**
     * Tell if the given locations are on the same block.
     *
     * @param loc1 The first location in question.
     * @param loc2 The second location in question.
     * @return If the location are on the same block.
     */
    public static boolean isSameBlock(final Location loc1, final Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) && loc1.getBlockX() == loc2.getBlockX()
               && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Tell if the chunk that holds the given location is
     * currently loaded.
     *
     * @param loc The location in question.
     * @return If the location's chunk is loaded.
     */
    public static boolean isChunkLoaded(final Location loc) {
        return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing.
     * This will include the main 4 directions and their diagonal
     * counterparts as well:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#NORTH_EAST}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#SOUTH_EAST}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#SOUTH_WEST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#NORTH_WEST}</li>
     * </ul>
     * It does not include {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * or any diagonal direction.
     * <p>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, true, false);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getDirection(final Location loc) {
        return getDirection(loc, true, false);
    }

    /**
     * Get the cardinal {@link BlockFace direction} that the location
     * is facing. Cardinal direction includes the 4 normal directions:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     * </ul>
     * It does not include {@link BlockFace#UP} or {@link BlockFace#DOWN}
     * or any diagonal direction.
     * <p>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, false, false);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getCardinalDirection(final Location loc) {
        return getDirection(loc, false, false);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing
     * including vertical and cardinal directions directions:
     * <ul>
     *     <li>{@link BlockFace#NORTH}</li>
     *     <li>{@link BlockFace#SOUTH}</li>
     *     <li>{@link BlockFace#EAST}</li>
     *     <li>{@link BlockFace#WEST}</li>
     *     <li>{@link BlockFace#UP}</li>
     *     <li>{@link BlockFace#DOWN}</li>
     * </ul>
     * This is equivalent to:
     * <pre>
     *     LocationUtil.getDirection(loc, false, true);</pre>
     *
     * @param loc The location to get the direction from.
     * @return The direction that the location is facing.
     * @see #getDirection(Location, boolean, boolean)
     */
    public static BlockFace getVerticalDirection(final Location loc) {
        return getDirection(loc, false, true);
    }

    /**
     * Get the {@link BlockFace direction} that the location is facing.
     * Optionally include directions such as {@link BlockFace#NORTH_EAST}
     * or {@link BlockFace#SOUTH_WEST} as well as {@link BlockFace#UP} or
     * {@link BlockFace#DOWN}.
     *
     * @param loc The location to get the direction off of.
     * @param diagonal If directions such as {@link BlockFace#NORTH_EAST} or
     *                 {@link BlockFace#NORTH_WEST} etc. should be included.
     * @param vertical If the {@link BlockFace#UP} and {@link BlockFace#DOWN}
     *                 directions should be included in the calculation.
     * @return The direction that the location is facing.
     */
    public static BlockFace getDirection(final Location loc, final boolean diagonal, final boolean vertical) {

        if (vertical) {

            final float pitch = loc.getPitch();
            if (pitch > 70) {
                return BlockFace.UP;
            }

            if (pitch < -70) {
                return BlockFace.DOWN;
            }
        }

        final BlockFace[] directions = diagonal ? DIAGONAL : CARDINAL;
        return directions[((int) loc.getYaw() + 585) % 360 / directions.length];
    }
}
