package org.cyclops.colossalchests.gametest;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.cyclopscore.gametest.GameTest;

/**
 * Game tests for Fabric-only behaviour.
 * @author rubensworks
 */
public class GameTestsFabric extends GameTestsCommon {

    /**
     * Two separate chests must never share a single item storage.
     * Regression test for <a href="https://github.com/CyclopsMC/ColossalChests/issues/208">#208</a>.
     */
    @GameTest(template = TEMPLATE_EMPTY)
    public void testColossalItemStorageIsPerChest(GameTestHelper helper) {
        BlockPos posFirst = new BlockPos(1, 0, 1);
        BlockPos posSecond = new BlockPos(5, 0, 1);
        BlockEntityColossalChest coreFirst = createChest(helper, posFirst, ChestMaterial.WOOD, 3);
        BlockEntityColossalChest coreSecond = createChest(helper, posSecond, ChestMaterial.WOOD, 3);

        // Look up the second chest first: both chests are empty and equally sized,
        // so a storage cache keyed on inventory contents would hand the first chest the second chest's storage.
        Storage<ItemVariant> storageSecond = ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(posSecond), null);
        Storage<ItemVariant> storageFirst = ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(posFirst), null);
        helper.assertTrue(storageFirst != null && storageSecond != null, "Chest cores expose no item storage");

        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertValueEqual(storageFirst.insert(ItemVariant.of(Items.APPLE), 64, transaction), 64L, "inserted item count");
            transaction.commit();
        }

        helper.assertFalse(coreFirst.getInventory().isEmpty(), "Inserted items did not end up in the targeted chest");
        helper.assertTrue(coreSecond.getInventory().isEmpty(), "Inserted items ended up in another chest");

        helper.succeed();
    }
}
