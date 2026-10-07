package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestChestBuilder {

    private static final BlockPos FEET = new BlockPos(0, 64, 0);

    @Test
    public void testInFrontSouth() {
        assertEquals(new ChestStructure(new BlockPos(-2, 64, 2), 5), ChestBuilder.inFront(FEET, Direction.SOUTH, 5));
    }

    @Test
    public void testInFrontNorth() {
        assertEquals(new ChestStructure(new BlockPos(-2, 64, -6), 5), ChestBuilder.inFront(FEET, Direction.NORTH, 5));
    }

    @Test
    public void testInFrontEast() {
        assertEquals(new ChestStructure(new BlockPos(2, 64, -2), 5), ChestBuilder.inFront(FEET, Direction.EAST, 5));
    }

    @Test
    public void testInFrontWest() {
        assertEquals(new ChestStructure(new BlockPos(-6, 64, -2), 5), ChestBuilder.inFront(FEET, Direction.WEST, 5));
    }

    @Test
    public void testInFrontEvenSize() {
        assertEquals(new ChestStructure(new BlockPos(-1, 64, 2), 4), ChestBuilder.inFront(FEET, Direction.SOUTH, 4));
    }

    @Test
    public void testCorePosFacesViewer() {
        ChestStructure structure = new ChestStructure(new BlockPos(-2, 64, 2), 5);
        assertEquals(new BlockPos(0, 66, 2), ChestBuilder.getCorePos(structure, Vec3.atCenterOf(FEET)));
        assertEquals(new BlockPos(0, 66, 6), ChestBuilder.getCorePos(structure, new Vec3(0, 64, 20)));
        assertEquals(new BlockPos(-2, 66, 4), ChestBuilder.getCorePos(structure, new Vec3(-20, 64, 4)));
        assertEquals(new BlockPos(2, 66, 4), ChestBuilder.getCorePos(structure, new Vec3(20, 64, 4)));
    }

    @Test
    public void testCorePosSize2() {
        ChestStructure structure = new ChestStructure(new BlockPos(0, 0, 0), 2);
        assertEquals(new BlockPos(1, 1, 0), ChestBuilder.getCorePos(structure, new Vec3(1, 1, -10)));
    }

}
