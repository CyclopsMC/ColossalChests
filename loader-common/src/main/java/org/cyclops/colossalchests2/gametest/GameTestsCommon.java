package org.cyclops.colossalchests2.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.config.ChestTables;
import org.cyclops.colossalchests2.config.ChestTablesLoader;

/**
 * @author rubensworks
 */
public class GameTestsCommon {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";
    public static final BlockPos POS = BlockPos.ZERO.offset(1, 0, 1);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testHarness(GameTestHelper helper) {
        // Proves the mod's template and classes load on this loader.
        helper.setBlock(POS, Blocks.CHEST);
        helper.assertBlockPresent(Blocks.CHEST, POS);
        helper.assertValueEqual(ChestTablesLoader.get().materials().size(), ChestTables.DEFAULT.materials().size(), "material count");
        helper.succeed();
    }

}
