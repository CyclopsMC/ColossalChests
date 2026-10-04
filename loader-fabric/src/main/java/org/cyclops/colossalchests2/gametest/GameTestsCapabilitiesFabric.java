package org.cyclops.colossalchests2.gametest;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.gametest.framework.GameTest;
import org.cyclops.colossalchests2.block.ChestMaterial;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.capability.ChestStorageFabric;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;

/**
 * Fabric item storage adapter tests through Fabric's own transfer helpers.
 * @author rubensworks
 */
public class GameTestsCapabilitiesFabric {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";

    private static final ItemVariant STONE = ItemVariant.of(Items.STONE);

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMoveFromVanillaContainer(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        SimpleContainer container = new SimpleContainer(27);
        for (int i = 0; i < 5; i++) {
            container.setItem(i, new ItemStack(Items.STONE, 64));
        }

        long moved = StorageUtil.move(InventoryStorage.of(container, null), new ChestStorageFabric(storage), variant -> true, Long.MAX_VALUE, null);

        helper.assertValueEqual(moved, 320L, "moved count");
        helper.assertValueEqual(storage.getSlot(0).getCount(), 256L, "slot 0 count");
        helper.assertValueEqual(storage.getSlot(1).getCount(), 64L, "slot 1 count");
        helper.assertTrue(container.isEmpty(), "Expected the source container to be empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMoveToVanillaContainer(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 200, false);
        SimpleContainer container = new SimpleContainer(27);

        long moved = StorageUtil.move(new ChestStorageFabric(storage), InventoryStorage.of(container, null), variant -> true, Long.MAX_VALUE, null);

        helper.assertValueEqual(moved, 200L, "moved count");
        helper.assertTrue(storage.getSlot(0).isEmpty(), "Expected the chest slot to be empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testAbortedTransactionLeavesStorageUntouched(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 10, false);
        ChestStorageFabric fabricStorage = new ChestStorageFabric(storage);

        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertValueEqual(fabricStorage.insert(STONE, 500, transaction), 500L, "inserted count");
            helper.assertValueEqual(fabricStorage.extract(STONE, 5, transaction), 5L, "extracted count");
            helper.assertValueEqual(fabricStorage.getSlot(2).insert(ItemVariant.of(Items.DIRT), 7, transaction), 7L, "slot insert count");
            // No commit: closing aborts.
        }

        helper.assertValueEqual(storage.getSlot(0).getCount(), 10L, "slot 0 count");
        helper.assertTrue(storage.getSlot(1).isEmpty(), "Expected slot 1 to be empty");
        helper.assertTrue(storage.getSlot(2).isEmpty(), "Expected slot 2 to be empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCommittedNestedTransaction(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        ChestStorageFabric fabricStorage = new ChestStorageFabric(storage);

        try (Transaction outer = Transaction.openOuter()) {
            fabricStorage.insert(STONE, 10, outer);
            try (Transaction inner = outer.openNested()) {
                fabricStorage.insert(STONE, 20, inner);
                // No commit: the nested transaction aborts.
            }
            outer.commit();
        }

        helper.assertValueEqual(storage.getSlot(0).getCount(), 10L, "slot 0 count");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testLongCounts(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(1L << 40).withMaxItemsPerSlot(Long.MAX_VALUE));
        ChestStorageFabric fabricStorage = new ChestStorageFabric(storage);
        long amount = Integer.MAX_VALUE + 10L;

        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertValueEqual(fabricStorage.insert(STONE, amount, transaction), amount, "inserted count");
            transaction.commit();
        }

        SingleSlotStorage<ItemVariant> slot = fabricStorage.getSlot(0);
        helper.assertValueEqual(slot.getAmount(), amount, "slot amount");
        helper.assertValueEqual(slot.getCapacity(), (1L << 40) * 64, "slot capacity");
        helper.assertTrue(slot.getResource().equals(STONE), "Expected stone in the slot");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testSlotExtractRejectsOtherTypes(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 10, false);
        ChestStorageFabric fabricStorage = new ChestStorageFabric(storage);

        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertValueEqual(fabricStorage.getSlot(0).extract(ItemVariant.of(Items.DIRT), 5, transaction), 0L, "extracted dirt");
            helper.assertValueEqual(fabricStorage.getSlot(0).extract(STONE, 5, transaction), 5L, "extracted stone");
            transaction.commit();
        }

        helper.assertValueEqual(storage.getSlot(0).getCount(), 5L, "slot count");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testRejectedTransfersLeaveStorageUntouched(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(1));
        ChestStorageFabric fabricStorage = new ChestStorageFabric(storage);
        SingleSlotStorage<ItemVariant> slot = fabricStorage.getSlot(0);
        helper.assertTrue(fabricStorage.getStorage() == storage, "Expected the wrapped storage");
        helper.assertValueEqual(fabricStorage.getSlotCount(), 1, "slot count");
        helper.assertTrue(slot.isResourceBlank() && slot.getResource().isBlank(), "Expected a blank slot");

        try (Transaction transaction = Transaction.openOuter()) {
            helper.assertValueEqual(fabricStorage.extract(STONE, 5, transaction), 0L, "extracted from empty storage");
            helper.assertValueEqual(slot.extract(STONE, 5, transaction), 0L, "extracted from empty slot");
            helper.assertValueEqual(slot.insert(STONE, 100, transaction), 64L, "inserted into slot");
            helper.assertValueEqual(slot.insert(STONE, 1, transaction), 0L, "inserted into full slot");
            helper.assertValueEqual(fabricStorage.insert(STONE, 1, transaction), 0L, "inserted into full storage");
            transaction.commit();
        }

        helper.assertValueEqual(storage.getSlot(0).getCount(), 64L, "slot count");
        helper.assertTrue(!slot.isResourceBlank() && slot.getResource().equals(STONE), "Expected stone in the slot");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testStorageOnFormedChest(GameTestHelper helper) {
        BlockPos min = new BlockPos(1, 1, 1);
        BlockPos corePos = GameTestsCommon.buildChest(helper, min, 3, ChestMaterial.WOOD);
        BlockPos wallPos = min.offset(1, 2, 1);
        helper.startSequence()
                .thenWaitUntil(() -> GameTestsCommon.assertFormed(helper, corePos, min, 3))
                .thenExecute(() -> {
                    Storage<ItemVariant> coreStorage = ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(corePos), Direction.NORTH);
                    Storage<ItemVariant> wallStorage = ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(wallPos), Direction.UP);
                    helper.assertTrue(coreStorage != null && coreStorage == wallStorage, "Expected the core and walls to share one storage");
                    try (Transaction transaction = Transaction.openOuter()) {
                        helper.assertValueEqual(wallStorage.insert(STONE, 10, transaction), 10L, "inserted through the wall");
                        transaction.commit();
                    }
                    helper.setBlock(min.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> GameTestsCommon.assertDormant(helper, corePos))
                .thenExecute(() -> {
                    helper.assertTrue(ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(corePos), Direction.NORTH) == null, "Expected no storage on a dormant core");
                    helper.assertTrue(ItemStorage.SIDED.find(helper.getLevel(), helper.absolutePos(wallPos), Direction.UP) == null, "Expected no storage on a dormant wall");
                    helper.assertValueEqual(GameTestsCommon.getCore(helper, corePos).getStorage().getSlot(0).getCount(), 10L, "kept count");
                })
                .thenSucceed();
    }

}
