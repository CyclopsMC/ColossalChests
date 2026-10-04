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

    private ChestShape() {
    }

    /**
     * The side the chest's lock faces, with the same rules as Colossal Chests 1: towards the side of the
     * structure the core is furthest out on, horizontally. Ties go to the z axis, except for 2x2 chests,
     * where every core is on a corner, which use the x axis unless the core is on the diagonal.
     * A core centered on the top or bottom faces south.
     * @param structure The structure.
     * @param core The core position.
     * @return A horizontal direction.
     */
    public static Direction getFacing(ChestStructure structure, BlockPos core) {
        double center = (structure.size() - 1) / 2D;
        // From the core to the structure center.
        double dx = structure.min().getX() + center - core.getX();
        double dz = structure.min().getZ() + center - core.getZ();
        Direction towardsCenter;
        if (Math.abs(dx) > Math.abs(dz) || (dx != dz && structure.size() == 2)) {
            towardsCenter = Math.round(dx) > 0 ? Direction.EAST : Direction.WEST;
        } else {
            towardsCenter = Math.round(dz) > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return towardsCenter.getOpposite();
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
