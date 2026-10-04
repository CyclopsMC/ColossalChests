package org.cyclops.colossalchests2.gametest;

import net.minecraft.gametest.framework.GameTest;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.block.WallType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.minecraft.world.SimpleContainer;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorage;
import org.cyclops.colossalchests2.capability.ItemHandlerLogic;
import org.cyclops.colossalchests2.modcompat.InventoryStateChestStorage;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;

/**
 * Item handler adapter tests through NeoForge's own transfer helpers.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
@PrefixGameTestTemplate(false)
public class GameTestsCapabilitiesNeoForge {

    public static final String TEMPLATE_EMPTY = "empty10";

    private static ChestStorage createStorage() {
        return new ChestStorage(3, CapacityProfile.ofDepth(4));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testInsertStackedMergesIntoMatchingSlot(GameTestHelper helper) {
        ChestStorage storage = createStorage();
        IItemHandler handler = new ItemHandlerChestStorage(new ItemHandlerLogic(storage));
        storage.insert(2, new ItemStack(Items.STONE), 10, false);

        ItemStack remainder = ItemHandlerHelper.insertItemStacked(handler, new ItemStack(Items.STONE, 64), false);

        helper.assertTrue(remainder.isEmpty(), "Expected no remainder");
        helper.assertValueEqual(storage.getSlot(2).getCount(), 74L, "merged count");
        helper.assertTrue(storage.getSlot(0).isEmpty(), "Expected slot 0 to stay empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testInsertSimulateLeavesStorageUntouched(GameTestHelper helper) {
        ChestStorage storage = createStorage();
        IItemHandler handler = new ItemHandlerChestStorage(new ItemHandlerLogic(storage));

        ItemStack remainder = ItemHandlerHelper.insertItem(handler, new ItemStack(Items.STONE, 64), true);

        helper.assertTrue(remainder.isEmpty(), "Expected the simulated insert to fit");
        helper.assertTrue(storage.getSlot(0).isEmpty(), "Expected no change after simulation");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMoveAllToVanillaContainer(GameTestHelper helper) {
        ChestStorage storage = createStorage();
        IItemHandler handler = new ItemHandlerChestStorage(new ItemHandlerLogic(storage));
        storage.insert(0, new ItemStack(Items.STONE), 200, false);
        IItemHandler target = new InvWrapper(new SimpleContainer(27));

        // Extract-all loop as automation does: one stack at a time until empty.
        int moved = 0;
        for (int i = 0; i < 100; i++) {
            ItemStack extracted = handler.extractItem(0, 64, false);
            if (extracted.isEmpty()) {
                break;
            }
            helper.assertTrue(ItemHandlerHelper.insertItemStacked(target, extracted, false).isEmpty(), "Target is full");
            moved += extracted.getCount();
        }

        helper.assertValueEqual(moved, 200, "moved count");
        helper.assertTrue(storage.getSlot(0).isEmpty(), "Expected the chest slot to be empty");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCountAboveIntClamped(GameTestHelper helper) {
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(1L << 40).withMaxItemsPerSlot(Long.MAX_VALUE));
        IItemHandler handler = new ItemHandlerChestStorage(new ItemHandlerLogic(storage));
        storage.insert(0, new ItemStack(Items.STONE), Integer.MAX_VALUE + 10L, false);

        helper.assertValueEqual(handler.getStackInSlot(0).getCount(), Integer.MAX_VALUE, "clamped count");
        helper.assertValueEqual(handler.getSlotLimit(0), Integer.MAX_VALUE, "clamped limit");
        helper.assertValueEqual(handler.extractItem(0, 64, false).getCount(), 64, "extracted count");
        helper.assertValueEqual(storage.getSlot(0).getCount(), Integer.MAX_VALUE + 10L - 64, "remaining count");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testInventoryStateChangesOnlyOnChange(GameTestHelper helper) {
        ChestStorage storage = createStorage();
        IItemHandler handler = new ItemHandlerChestStorage(new ItemHandlerLogic(storage));
        IInventoryState state = new InventoryStateChestStorage(storage);

        int initial = state.getState();
        handler.insertItem(0, new ItemStack(Items.STONE, 5), true);
        helper.assertValueEqual(state.getState(), initial, "state after simulation");
        handler.insertItem(0, new ItemStack(Items.STONE, 5), false);
        int afterInsert = state.getState();
        helper.assertTrue(afterInsert != initial, "Expected the state to change after an insert");
        handler.extractItem(0, 1, false);
        helper.assertTrue(state.getState() != afterInsert, "Expected the state to change after an extract");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCapabilitiesOnFormedChest(GameTestHelper helper) {
        BlockPos min = new BlockPos(1, 1, 1);
        BlockPos corePos = GameTestsCommon.buildChest(helper, min, 3, ChestMaterial.WOOD);
        BlockPos wallPos = GameTestsCommon.placeWall(helper, min.offset(1, 2, 1), WallType.INTERFACE);
        BlockPos plainWallPos = min.offset(2, 1, 1);
        BlockPos brokenWall = min.offset(0, 1, 1);
        helper.startSequence()
                .thenWaitUntil(() -> GameTestsCommon.assertFormed(helper, corePos, min, 3))
                .thenExecute(() -> {
                    IItemHandler coreHandler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(corePos), Direction.NORTH);
                    IItemHandler wallHandler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(wallPos), Direction.UP);
                    IInventoryState state = helper.getLevel().getCapability(org.cyclops.commoncapabilities.api.capability.Capabilities.InventoryState.BLOCK, helper.absolutePos(wallPos), Direction.UP);
                    helper.assertTrue(coreHandler != null && wallHandler != null && state != null, "Expected capabilities on a formed chest");
                    helper.assertTrue(helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(plainWallPos), Direction.EAST) == null,
                            "Expected no item handler on a plain wall");
                    int initialState = state.getState();
                    helper.assertTrue(wallHandler.insertItem(0, new ItemStack(Items.STONE, 10), false).isEmpty(), "Expected the wall to accept items");
                    helper.assertValueEqual(coreHandler.getStackInSlot(0).getCount(), 10, "count through the core");
                    helper.assertTrue(state.getState() != initialState, "Expected the inventory state to change");
                    helper.setBlock(brokenWall, Blocks.AIR);
                })
                .thenWaitUntil(() -> GameTestsCommon.assertDormant(helper, corePos))
                .thenExecute(() -> {
                    helper.assertTrue(helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(corePos), Direction.NORTH) == null, "Expected no item handler on a dormant core");
                    helper.assertTrue(helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(wallPos), Direction.UP) == null, "Expected no item handler on a dormant wall");
                    helper.assertTrue(helper.getLevel().getCapability(org.cyclops.commoncapabilities.api.capability.Capabilities.InventoryState.BLOCK, helper.absolutePos(corePos), Direction.NORTH) == null, "Expected no inventory state on a dormant core");
                })
                .thenSucceed();
    }

}
