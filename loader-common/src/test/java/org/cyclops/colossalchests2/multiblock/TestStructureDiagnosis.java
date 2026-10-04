package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestStructureDiagnosis {

    private static final BlockPos MIN = new BlockPos(10, 20, 30);
    private static final BlockPos CORE = MIN.offset(1, 1, 0);

    private static StructureDiagnosis.Result diagnose(GridView view, BlockPos start) {
        return StructureDiagnosis.diagnose(view, start, material -> material.equals("wood") ? 3 : 5);
    }

    @Test
    public void testNotAChestBlock() {
        StructureDiagnosis.Result result = diagnose(new GridView(), MIN);
        assertEquals(StructureDiagnosis.Problem.NONE, result.getProblem());
        assertEquals(StructureDiagnosis.Result.EMPTY, result);
    }

    @Test
    public void testValidStructureHasNoProblem() {
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood");
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.NONE, result.getProblem());
        assertEquals(CORE, result.getCore());
    }

    @Test
    public void testFunctionalWallsAreNoWrongMaterial() {
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood").functionalWall(MIN).functionalWall(MIN.offset(2, 2, 2));
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.NONE, result.getProblem());
        assertEquals(CORE, result.getCore());
    }

    @Test
    public void testNoCoreFromFunctionalWall() {
        // The material comes from the plain walls, not the functional wall the diagnosis starts at.
        GridView view = new GridView().cube(MIN, 3, "wood").functionalWall(MIN);
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.NO_CORE, result.getProblem());
        assertEquals(3, result.maxSize());
    }

    @Test
    public void testNoCore() {
        GridView view = new GridView().cube(MIN, 3, "wood");
        assertEquals(StructureDiagnosis.Problem.NO_CORE, diagnose(view, MIN).getProblem());
    }

    @Test
    public void testMultipleCores() {
        BlockPos otherCore = MIN.offset(1, 2, 1);
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood").core(otherCore, "wood");
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.MULTIPLE_CORES, result.getProblem());
        assertEquals(Set.of(CORE, otherCore), Set.copyOf(result.getProblemPositions()));
    }

    @Test
    public void testNotCube() {
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood").wall(MIN.offset(0, 3, 0), "wood");
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.NOT_CUBE, result.getProblem());
        assertEquals(3, result.sizeX());
        assertEquals(4, result.sizeY());
        assertEquals(3, result.sizeZ());
    }

    @Test
    public void testTooSmall() {
        GridView view = new GridView().core(MIN, "wood");
        assertEquals(StructureDiagnosis.Problem.TOO_SMALL, diagnose(view, MIN).getProblem());
    }

    @Test
    public void testTooLarge() {
        GridView view = new GridView().cube(MIN, 4, "wood").core(CORE, "wood");
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.TOO_LARGE, result.getProblem());
        assertEquals(3, result.maxSize());
        assertTrue(result.getProblemPositions().isEmpty());
    }

    @Test
    public void testLargerMaterialAllowsSize() {
        GridView view = new GridView().cube(MIN, 4, "iron").core(CORE, "iron");
        assertEquals(StructureDiagnosis.Problem.NONE, diagnose(view, MIN).getProblem());
    }

    @Test
    public void testMissingWrongMaterialAndObstruction() {
        BlockPos missing = MIN.offset(2, 2, 2);
        BlockPos wrong = MIN.offset(0, 1, 1);
        BlockPos inside = MIN.offset(1, 1, 1);
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood")
                .clear(missing).wall(wrong, "iron").solid(inside);
        StructureDiagnosis.Result result = diagnose(view, MIN);
        assertEquals(StructureDiagnosis.Problem.BLOCKS, result.getProblem());
        assertEquals(List.of(missing), result.missing());
        assertEquals(List.of(wrong), result.wrongMaterial());
        assertEquals(List.of(inside), result.obstructions());
        assertEquals(Set.of(missing, wrong, inside), Set.copyOf(result.getProblemPositions()));
    }

    @Test
    public void testCoreMaterialWinsOverClickedWall() {
        // Clicking an iron wall in a wooden chest reports that wall, not all the wooden ones.
        BlockPos wrong = MIN.offset(0, 1, 1);
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood").wall(wrong, "iron");
        StructureDiagnosis.Result result = diagnose(view, wrong);
        assertEquals(List.of(wrong), result.wrongMaterial());
    }

    @Test
    public void testUnloadedPositionsAreSkipped() {
        BlockPos missing = MIN.offset(2, 2, 2);
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood").clear(missing).unload(missing);
        assertEquals(StructureDiagnosis.Problem.NONE, diagnose(view, MIN).getProblem());
    }

    @Test
    public void testNeighbouringChestIsSeparateWhenNotTouching() {
        GridView view = new GridView().cube(MIN, 3, "wood").core(CORE, "wood")
                .cube(MIN.offset(4, 0, 0), 3, "wood");
        assertEquals(StructureDiagnosis.Problem.NONE, diagnose(view, MIN).getProblem());
        assertEquals(StructureDiagnosis.Problem.NO_CORE, diagnose(view, MIN.offset(4, 0, 0)).getProblem());
    }

}
