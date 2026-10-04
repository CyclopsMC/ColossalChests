package org.cyclops.colossalchests2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.cyclops.colossalchests2.multiblock.StructureDiagnosis;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestInteractionsMessages extends BootstrapTest {

    private static final List<BlockPos> ONE = List.of(BlockPos.ZERO);
    private static final List<BlockPos> TWO = List.of(BlockPos.ZERO, BlockPos.ZERO.above());

    private static TranslatableContents message(StructureDiagnosis.Result result) {
        return (TranslatableContents) ChestInteractions.getMessage(result).getContents();
    }

    private static StructureDiagnosis.Result result(int x, int y, int z, List<BlockPos> cores, List<BlockPos> missing) {
        return new StructureDiagnosis.Result(x, y, z, 3, cores, missing, List.of(), List.of());
    }

    @Test
    public void testMessages() {
        assertEquals("chest.colossalchests2.diagnosis.none", message(StructureDiagnosis.Result.EMPTY).getKey());
        assertEquals("chest.colossalchests2.diagnosis.no_core", message(result(3, 3, 3, List.of(), List.of())).getKey());
        assertEquals("chest.colossalchests2.diagnosis.multiple_cores", message(result(3, 3, 3, TWO, List.of())).getKey());
        assertArrayEquals(new Object[]{2}, message(result(3, 3, 3, TWO, List.of())).getArgs());
        assertEquals("chest.colossalchests2.diagnosis.not_cube", message(result(3, 4, 3, ONE, List.of())).getKey());
        assertArrayEquals(new Object[]{3, 4, 3}, message(result(3, 4, 3, ONE, List.of())).getArgs());
        assertEquals("chest.colossalchests2.diagnosis.too_small", message(result(1, 1, 1, ONE, List.of())).getKey());
        assertEquals("chest.colossalchests2.diagnosis.too_large", message(result(4, 4, 4, ONE, List.of())).getKey());
        assertArrayEquals(new Object[]{4, 3}, message(result(4, 4, 4, ONE, List.of())).getArgs());
        assertEquals("chest.colossalchests2.diagnosis.blocks", message(result(3, 3, 3, ONE, TWO)).getKey());
        assertArrayEquals(new Object[]{2, 0, 0}, message(result(3, 3, 3, ONE, TWO)).getArgs());
    }

}
