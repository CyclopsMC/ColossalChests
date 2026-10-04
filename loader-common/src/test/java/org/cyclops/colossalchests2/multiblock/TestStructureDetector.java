package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestStructureDetector {

    private static final BlockPos MIN = new BlockPos(10, 20, 30);

    private static StructureDetector.Result detect(GridView view, BlockPos core) {
        return StructureDetector.detect(view, core, 10);
    }

    private static void assertValid(StructureDetector.Result result, BlockPos min, int size) {
        assertEquals(StructureDetector.Result.State.VALID, result.state());
        assertEquals(new ChestStructure(min, size), result.structure());
    }

    @Test
    public void testValidCubeCoreOnFace() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood");
        assertValid(detect(view, core), MIN, 3);
    }

    @Test
    public void testValidCubeCoreOnEdge() {
        BlockPos core = MIN.offset(0, 2, 1);
        GridView view = new GridView().cube(MIN, 4, "wood").core(core, "wood");
        assertValid(detect(view, core), MIN, 4);
    }

    @Test
    public void testValidCubeCoreOnCorner() {
        BlockPos core = MIN.offset(4, 4, 4);
        GridView view = new GridView().cube(MIN, 5, "wood").core(core, "wood");
        assertValid(detect(view, core), MIN, 5);
    }

    @Test
    public void testValidCubeOnEveryCoreFace() {
        for (BlockPos core : new BlockPos[]{MIN.offset(1, 1, 0), MIN.offset(1, 1, 2), MIN.offset(0, 1, 1),
                MIN.offset(2, 1, 1), MIN.offset(1, 0, 1), MIN.offset(1, 2, 1)}) {
            GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood");
            assertValid(detect(view, core), MIN, 3);
        }
    }

    @Test
    public void testValidSmallest() {
        BlockPos core = MIN;
        GridView view = new GridView().cube(MIN, 2, "wood").core(core, "wood");
        assertValid(detect(view, core), MIN, 2);
    }

    @Test
    public void testValidLargest() {
        BlockPos core = MIN.offset(5, 9, 5);
        GridView view = new GridView().cube(MIN, 10, "wood").core(core, "wood");
        assertValid(detect(view, core), MIN, 10);
    }

    @Test
    public void testMissingCorner() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").clear(MIN.offset(2, 2, 2));
        assertEquals(StructureDetector.Result.INVALID, detect(view, core));
    }

    @Test
    public void testMissingWall() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").clear(MIN.offset(1, 2, 1));
        assertEquals(StructureDetector.Result.INVALID, detect(view, core));
    }

    @Test
    public void testTwoCores() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").core(MIN.offset(1, 1, 2), "wood");
        assertEquals(StructureDetector.Result.INVALID, detect(view, core));
    }

    @Test
    public void testMixedMaterials() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").wall(MIN.offset(2, 2, 2), "iron");
        assertEquals(StructureDetector.Result.INVALID, detect(view, core));
    }

    @Test
    public void testCoreOfOtherMaterial() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "iron");
        assertEquals(StructureDetector.Result.INVALID, detect(view, core));
    }

    @Test
    public void testOversize() {
        BlockPos core = MIN.offset(2, 2, 0);
        GridView view = new GridView().cube(MIN, 5, "wood").core(core, "wood");
        assertEquals(StructureDetector.Result.INVALID, StructureDetector.detect(view, core, 4));
        assertValid(StructureDetector.detect(view, core, 5), MIN, 5);
    }

    @Test
    public void testOversizeBeyondHardCap() {
        BlockPos core = MIN.offset(5, 5, 0);
        GridView view = new GridView().cube(MIN, 11, "wood").core(core, "wood");
        assertEquals(StructureDetector.Result.INVALID, StructureDetector.detect(view, core, 20));
    }

    @Test
    public void testNonHollow() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").solid(MIN.offset(1, 1, 1));
        assertEquals(StructureDetector.Result.INVALID, detect(view, core));
    }

    @Test
    public void testNonHollowWithWallFormsSmallerCube() {
        // A wall in the interior makes the 3x3 invalid, but completes a valid 2x2 cube around the core.
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").wall(MIN.offset(1, 1, 1), "wood");
        assertValid(detect(view, core), MIN.offset(1, 1, 0), 2);
    }

    @Test
    public void testNotACore() {
        GridView view = new GridView().cube(MIN, 3, "wood");
        assertEquals(StructureDetector.Result.INVALID, detect(view, MIN.offset(1, 1, 0)));
        assertEquals(StructureDetector.Result.INVALID, detect(view, MIN.offset(-5, 0, 0)));
    }

    @Test
    public void testAdjacentChestsBothForm() {
        BlockPos coreA = MIN.offset(1, 1, 0);
        BlockPos minB = MIN.offset(3, 0, 0);
        BlockPos coreB = minB.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(coreA, "wood").cube(minB, 3, "wood").core(coreB, "wood");
        assertValid(detect(view, coreA), MIN, 3);
        assertValid(detect(view, coreB), minB, 3);
    }

    @Test
    public void testStrayWallsAroundDoNotBreakFormation() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood")
                .wall(MIN.offset(-1, 1, 0), "wood").wall(MIN.offset(1, 3, 1), "wood").wall(MIN.offset(1, 1, -1), "wood");
        assertValid(detect(view, core), MIN, 3);
    }

    /**
     * A 3x3 and a 2x2 cube that only share the core, on opposite sides of it.
     */
    private static GridView twoCubesSharingCore() {
        return new GridView().cube(MIN, 3, "wood").cube(MIN.offset(-1, -1, -1), 2, "wood").core(MIN, "wood");
    }

    @Test
    public void testPrefersLargestCube() {
        assertValid(detect(twoCubesSharingCore(), MIN), MIN, 3);
    }

    @Test
    public void testUnloadedChunk() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").unload(MIN.offset(2, 2, 2));
        assertEquals(StructureDetector.Result.UNLOADED, detect(view, core));
    }

    @Test
    public void testUnloadedLargerCandidateDefers() {
        // The 2x2 is valid, but the 3x3 may also be once its far corner loads.
        GridView view = twoCubesSharingCore().unload(MIN.offset(2, 2, 2));
        assertEquals(StructureDetector.Result.UNLOADED, detect(view, MIN));
    }

    @Test
    public void testUnloadedOutsideCandidatesIgnored() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood").unload(MIN.offset(-3, 1, 0));
        assertValid(detect(view, core), MIN, 3);
    }

    @Test
    public void testFunctionalWallsFitAnyMaterial() {
        for (String material : new String[]{"wood", "iron"}) {
            BlockPos core = MIN.offset(1, 1, 0);
            // On the core's own lines too, which detection walks to find the size.
            GridView view = new GridView().cube(MIN, 3, material).core(core, material)
                    .functionalWall(MIN.offset(0, 1, 0)).functionalWall(MIN.offset(1, 2, 0)).functionalWall(MIN.offset(1, 1, 2));
            assertValid(detect(view, core), MIN, 3);
        }
    }

    @Test
    public void testOnlyFunctionalWalls() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView();
        for (BlockPos pos : new ChestStructure(MIN, 3).shell()) {
            view.functionalWall(pos);
        }
        assertValid(detect(view.core(core, "wood"), core), MIN, 3);
    }

    @Test
    public void testFunctionalWallsDoNotHideOtherMaterials() {
        BlockPos core = MIN.offset(1, 1, 0);
        GridView view = new GridView().cube(MIN, 3, "wood").core(core, "wood")
                .functionalWall(MIN.offset(0, 1, 0)).wall(MIN.offset(2, 2, 2), "iron");
        assertEquals(StructureDetector.Result.State.INVALID, detect(view, core).state());
    }

    @Test
    public void testStructureGeometry() {
        ChestStructure structure = new ChestStructure(MIN, 3);
        assertEquals(MIN.offset(2, 2, 2), structure.max());
        assertTrue(structure.contains(MIN.offset(1, 1, 1)));
        assertFalse(structure.contains(MIN.offset(3, 1, 1)));
        assertTrue(structure.isOnShell(MIN.offset(1, 1, 0)));
        assertFalse(structure.isOnShell(MIN.offset(1, 1, 1)));
        assertFalse(structure.isOnShell(MIN.offset(-1, 1, 1)));
        int count = 0;
        for (BlockPos pos : structure.shell()) {
            assertTrue(structure.isOnShell(pos));
            count++;
        }
        assertEquals(26, count);
    }

}
