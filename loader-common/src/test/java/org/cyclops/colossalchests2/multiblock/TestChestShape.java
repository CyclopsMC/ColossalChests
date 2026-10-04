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
        assertEquals(Direction.NORTH, ChestShape.getFacing(SIZE_3, MIN.offset(2, 1, 0)));
    }

    @Test
    public void testFacingSize2LikeColossalChests1() {
        // Off the diagonal, the x axis wins.
        assertEquals(Direction.EAST, ChestShape.getFacing(SIZE_2, MIN.offset(1, 1, 0)));
        assertEquals(Direction.WEST, ChestShape.getFacing(SIZE_2, MIN.offset(0, 0, 1)));
        // On the diagonal, the z axis wins.
        assertEquals(Direction.NORTH, ChestShape.getFacing(SIZE_2, MIN.offset(0, 1, 0)));
        assertEquals(Direction.SOUTH, ChestShape.getFacing(SIZE_2, MIN.offset(1, 0, 1)));
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
    public void testRenderBounds() {
        // Half the size around the structure, room for the opening lid.
        assertEquals(new AABB(8.5, 18.5, 28.5, 14.5, 24.5, 34.5), ChestShape.getRenderBounds(SIZE_3));
    }

}
