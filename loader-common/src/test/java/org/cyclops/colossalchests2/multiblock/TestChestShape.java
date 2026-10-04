package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestShape {

    private static final BlockPos MIN = new BlockPos(10, 20, 30);
    private static final ChestStructure SIZE_3 = new ChestStructure(MIN, 3);
    private static final ChestStructure SIZE_2 = new ChestStructure(MIN, 2);
    private static final ChestStructure SIZE_5 = new ChestStructure(MIN, 5);

    @Test
    public void testFacingCoreOnSides() {
        assertEquals(Direction.NORTH, ChestShape.getFacing(SIZE_3, MIN.offset(1, 1, 0)));
        assertEquals(Direction.SOUTH, ChestShape.getFacing(SIZE_3, MIN.offset(1, 1, 2)));
        assertEquals(Direction.WEST, ChestShape.getFacing(SIZE_3, MIN.offset(0, 1, 1)));
        assertEquals(Direction.EAST, ChestShape.getFacing(SIZE_3, MIN.offset(2, 1, 1)));
    }

    @Test
    public void testFacingCoreOffCenterOnSide() {
        assertEquals(Direction.EAST, ChestShape.getFacing(SIZE_5, MIN.offset(4, 0, 3)));
        assertEquals(Direction.NORTH, ChestShape.getFacing(SIZE_5, MIN.offset(1, 3, 0)));
    }

    @Test
    public void testFacingTiesPreferZ() {
        assertEquals(Direction.SOUTH, ChestShape.getFacing(SIZE_3, MIN.offset(2, 0, 2)));
        assertEquals(Direction.NORTH, ChestShape.getFacing(SIZE_3, MIN.offset(0, 2, 0)));
        assertEquals(Direction.NORTH, ChestShape.getFacing(SIZE_2, MIN.offset(1, 1, 0)));
        assertEquals(Direction.SOUTH, ChestShape.getFacing(SIZE_2, MIN.offset(0, 0, 1)));
    }

    @Test
    public void testFacingCoreCenteredOnTopOrBottom() {
        assertEquals(Direction.SOUTH, ChestShape.getFacing(SIZE_3, MIN.offset(1, 2, 1)));
        assertEquals(Direction.SOUTH, ChestShape.getFacing(SIZE_3, MIN.offset(1, 0, 1)));
    }

    @Test
    public void testIsOnLid() {
        // Seam at 3 * 9 / 14 = 1.93: only the top layer.
        assertFalse(ChestShape.isOnLid(SIZE_3, MIN.offset(0, 0, 0)));
        assertFalse(ChestShape.isOnLid(SIZE_3, MIN.offset(0, 1, 0)));
        assertTrue(ChestShape.isOnLid(SIZE_3, MIN.offset(0, 2, 0)));
        // Seam at 2 * 9 / 14 = 1.29.
        assertFalse(ChestShape.isOnLid(SIZE_2, MIN));
        assertTrue(ChestShape.isOnLid(SIZE_2, MIN.offset(0, 1, 0)));
        // Seam at 5 * 9 / 14 = 3.21.
        assertFalse(ChestShape.isOnLid(SIZE_5, MIN.offset(0, 2, 0)));
        assertTrue(ChestShape.isOnLid(SIZE_5, MIN.offset(0, 3, 0)));
    }

    @Test
    public void testOuterFaces() {
        assertEquals(List.of(Direction.NORTH), ChestShape.getOuterFaces(SIZE_3, MIN.offset(1, 1, 0)));
        assertEquals(List.of(Direction.DOWN, Direction.NORTH, Direction.WEST), ChestShape.getOuterFaces(SIZE_3, MIN));
        assertEquals(List.of(Direction.UP, Direction.SOUTH, Direction.EAST), ChestShape.getOuterFaces(SIZE_3, MIN.offset(2, 2, 2)));
        assertTrue(ChestShape.getOuterFaces(SIZE_3, MIN.offset(1, 1, 1)).isEmpty());
        assertTrue(ChestShape.getOuterFaces(SIZE_3, MIN.offset(-1, 1, 1)).isEmpty());
    }

    @Test
    public void testFrontPos() {
        assertEquals(MIN.offset(1, 1, -1), ChestShape.getFrontPos(SIZE_3, Direction.NORTH));
        assertEquals(MIN.offset(1, 1, 3), ChestShape.getFrontPos(SIZE_3, Direction.SOUTH));
        assertEquals(MIN.offset(-1, 1, 1), ChestShape.getFrontPos(SIZE_3, Direction.WEST));
        assertEquals(MIN.offset(3, 1, 1), ChestShape.getFrontPos(SIZE_3, Direction.EAST));
        assertEquals(MIN.offset(2, 1, 1), ChestShape.getFrontPos(SIZE_2, Direction.EAST));
        assertEquals(MIN.offset(1, 1, -1), ChestShape.getFrontPos(SIZE_2, Direction.NORTH));
    }

    @Test
    public void testLockDepth() {
        assertEquals(3D / 14D, ChestShape.getLockDepth(SIZE_3), 1e-9);
    }

    @Test
    public void testCoveredByLock() {
        // Size 3: the lock spans x 1.29 to 1.71 and y 1.29 to 2.14, so only the middle column, rows 1 and 2.
        assertTrue(ChestShape.isCoveredByLock(SIZE_3, Direction.NORTH, MIN.offset(1, 1, 0)));
        assertTrue(ChestShape.isCoveredByLock(SIZE_3, Direction.NORTH, MIN.offset(1, 2, 0)));
        assertFalse(ChestShape.isCoveredByLock(SIZE_3, Direction.NORTH, MIN.offset(1, 0, 0)));
        assertFalse(ChestShape.isCoveredByLock(SIZE_3, Direction.NORTH, MIN.offset(0, 1, 0)));
        // Only on the front face.
        assertFalse(ChestShape.isCoveredByLock(SIZE_3, Direction.NORTH, MIN.offset(1, 1, 2)));
        assertTrue(ChestShape.isCoveredByLock(SIZE_3, Direction.SOUTH, MIN.offset(1, 1, 2)));
        assertTrue(ChestShape.isCoveredByLock(SIZE_3, Direction.EAST, MIN.offset(2, 1, 1)));
        assertFalse(ChestShape.isCoveredByLock(SIZE_3, Direction.EAST, MIN.offset(2, 1, 0)));
        // Size 2: the lock spans x 0.86 to 1.14, so both columns, and y 0.86 to 1.43, so both rows.
        assertTrue(ChestShape.isCoveredByLock(SIZE_2, Direction.WEST, MIN.offset(0, 0, 0)));
        assertTrue(ChestShape.isCoveredByLock(SIZE_2, Direction.WEST, MIN.offset(0, 1, 1)));
    }

    @Test
    public void testRenderBounds() {
        // Half the size around the structure, room for the opening lid.
        assertEquals(new AABB(8.5, 18.5, 28.5, 14.5, 24.5, 34.5), ChestShape.getRenderBounds(SIZE_3));
    }

}
