package org.cyclops.colossalchests2.multiblock;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Geometry of the giant chest that a formed structure renders as.
 * The chest body exactly fills the structure's outer bounds, so every outer block face lies on the chest surface.
 * @author rubensworks
 */
public final class ChestShape {

    /**
     * Height of the lid hinge, as a fraction of the structure size.
     * The vanilla chest model is 14 pixels high with its lid starting at 9.
     */
    public static final double LID_SEAM = 9D / 14D;

    /**
     * The lock on the chest front, as fractions of the structure size, from the 14 pixel vanilla chest model:
     * 2 pixels wide, from 6 to 10 pixels high, and 1 pixel deep.
     */
    public static final double LOCK_HALF_WIDTH = 1D / 14D;
    public static final double LOCK_BOTTOM = 6D / 14D;
    public static final double LOCK_TOP = 10D / 14D;
    public static final double LOCK_DEPTH = 1D / 14D;

    private ChestShape() {
    }

    /**
     * @param structure The structure.
     * @return How far the lock sticks out of the chest front, in blocks.
     */
    public static double getLockDepth(ChestStructure structure) {
        return structure.size() * LOCK_DEPTH;
    }

    /**
     * @param structure The structure.
     * @param facing The chest facing.
     * @param pos A position on the shell.
     * @return If the closed lock covers part of the block's front face, so things drawn on that face must go
     *         on the front of the lock instead.
     */
    public static boolean isCoveredByLock(ChestStructure structure, Direction facing, BlockPos pos) {
        int size = structure.size();
        Direction.Axis frontAxis = facing.getAxis();
        int front = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE
                ? structure.min().get(frontAxis) + size - 1
                : structure.min().get(frontAxis);
        if (!structure.isOnShell(pos) || pos.get(frontAxis) != front) {
            return false;
        }
        Direction.Axis sideAxis = facing.getClockWise().getAxis();
        double center = structure.min().get(sideAxis) + size / 2D;
        double halfWidth = size * LOCK_HALF_WIDTH;
        double bottom = structure.min().getY() + size * LOCK_BOTTOM;
        double top = structure.min().getY() + size * LOCK_TOP;
        int side = pos.get(sideAxis);
        return side < center + halfWidth && side + 1 > center - halfWidth && pos.getY() < top && pos.getY() + 1 > bottom;
    }

    /**
     * The side the chest's lock faces: the horizontal side of the structure the core is closest to.
     * Ties prefer the z axis, and a core centered on the top or bottom faces south.
     * @param structure The structure.
     * @param core The core position.
     * @return A horizontal direction.
     */
    public static Direction getFacing(ChestStructure structure, BlockPos core) {
        double center = (structure.size() - 1) / 2D;
        double dx = core.getX() - structure.min().getX() - center;
        double dz = core.getZ() - structure.min().getZ() - center;
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        if (dz == 0) {
            return Direction.SOUTH;
        }
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    /**
     * @param structure The structure.
     * @param pos A position on the shell.
     * @return If the block is part of the lid, so it moves when the lid opens.
     */
    public static boolean isOnLid(ChestStructure structure, BlockPos pos) {
        return pos.getY() - structure.min().getY() + 0.5D > structure.size() * LID_SEAM;
    }

    /**
     * @param structure The structure.
     * @param pos A position on the shell.
     * @return The faces of the block that are on the outside of the structure.
     */
    public static List<Direction> getOuterFaces(ChestStructure structure, BlockPos pos) {
        List<Direction> faces = Lists.newArrayListWithCapacity(3);
        if (structure.isOnShell(pos)) {
            for (Direction direction : Direction.values()) {
                if (!structure.contains(pos.relative(direction))) {
                    faces.add(direction);
                }
            }
        }
        return faces;
    }

    /**
     * @param structure The structure.
     * @return The area the giant chest can draw in, including its lid while open.
     */
    public static AABB getRenderBounds(ChestStructure structure) {
        return new AABB(Vec3.atLowerCornerOf(structure.min()), Vec3.atLowerCornerOf(structure.max().offset(1, 1, 1)))
                .inflate(structure.size() / 2D);
    }

    /**
     * @param structure The structure.
     * @param facing The chest facing.
     * @return The position just outside the center of the chest's front, where the chest takes its light from.
     */
    public static BlockPos getFrontPos(ChestStructure structure, Direction facing) {
        int half = structure.size() / 2;
        BlockPos center = structure.min().offset(half, half, half);
        int distance = facing.getAxisDirection() == Direction.AxisDirection.POSITIVE ? structure.size() - half : half + 1;
        return center.relative(facing, distance);
    }

}
