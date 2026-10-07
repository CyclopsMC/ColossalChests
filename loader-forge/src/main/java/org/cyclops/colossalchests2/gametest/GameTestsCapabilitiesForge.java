package org.cyclops.colossalchests2.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorageForge;
import org.cyclops.colossalchests2.capability.ItemHandlerLogic;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;

/**
 * Item handler adapter tests through Forge's own transfer helpers.
 * @author rubensworks
 */
@GameTestHolder(Reference.MOD_ID)
public class GameTestsCapabilitiesForge {

    public static final String TEMPLATE_EMPTY = Reference.MOD_ID + ":empty10";

    private static ChestStorage createStorage() {
        return new ChestStorage(3, CapacityProfile.ofDepth(4));
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testInsertStackedMergesIntoMatchingSlot(GameTestHelper helper) {
        ChestStorage storage = createStorage();
        IItemHandler handler = new ItemHandlerChestStorageForge(new ItemHandlerLogic(storage));
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
        IItemHandler handler = new ItemHandlerChestStorageForge(new ItemHandlerLogic(storage));

        ItemStack remainder = ItemHandlerHelper.insertItem(handler, new ItemStack(Items.STONE, 64), true);

        helper.assertTrue(remainder.isEmpty(), "Expected the simulated insert to fit");
        helper.assertTrue(storage.getSlot(0).isEmpty(), "Expected no change after simulation");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testMoveAllToVanillaContainer(GameTestHelper helper) {
        ChestStorage storage = createStorage();
        IItemHandler handler = new ItemHandlerChestStorageForge(new ItemHandlerLogic(storage));
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
        IItemHandler handler = new ItemHandlerChestStorageForge(new ItemHandlerLogic(storage));
        storage.insert(0, new ItemStack(Items.STONE), Integer.MAX_VALUE + 10L, false);

        helper.assertValueEqual(handler.getStackInSlot(0).getCount(), Integer.MAX_VALUE, "clamped count");
        helper.assertValueEqual(handler.getSlotLimit(0), Integer.MAX_VALUE, "clamped limit");
        helper.assertValueEqual(handler.extractItem(0, 64, false).getCount(), 64, "extracted count");
        helper.assertValueEqual(storage.getSlot(0).getCount(), Integer.MAX_VALUE + 10L - 64, "remaining count");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCapabilityOnFormedCore(GameTestHelper helper) {
        BlockPos min = new BlockPos(1, 1, 1);
        BlockPos corePos = GameTestsCommon.buildChest(helper, min, 3, ChestMaterial.WOOD);
        helper.startSequence()
                .thenWaitUntil(() -> GameTestsCommon.assertFormed(helper, corePos, min, 3))
                .thenExecute(() -> {
                    IItemHandler handler = helper.getBlockEntity(corePos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).orElse(null);
                    helper.assertTrue(handler != null, "Expected an item handler on a formed core");
                    helper.assertTrue(handler.insertItem(0, new ItemStack(Items.STONE, 10), false).isEmpty(), "Expected the core to accept items");
                    helper.setBlock(min.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> GameTestsCommon.assertDormant(helper, corePos))
                .thenExecute(() -> helper.assertFalse(helper.getBlockEntity(corePos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).isPresent(), "Expected no item handler on a dormant core"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testCapabilityOnInterfaceWall(GameTestHelper helper) {
        BlockPos min = new BlockPos(1, 1, 1);
        BlockPos corePos = GameTestsCommon.buildChest(helper, min, 3, ChestMaterial.WOOD);
        BlockPos wallPos = GameTestsCommon.placeWall(helper, min.offset(1, 2, 1), WallType.INTERFACE);
        LazyOptional<IItemHandler>[] first = new LazyOptional[1];
        helper.startSequence()
                .thenWaitUntil(() -> GameTestsCommon.assertFormed(helper, corePos, min, 3))
                .thenExecute(() -> {
                    first[0] = helper.getBlockEntity(wallPos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
                    IItemHandler handler = first[0].orElse(null);
                    helper.assertTrue(handler != null, "Expected an item handler on a formed interface");
                    helper.assertTrue(handler.insertItem(0, new ItemStack(Items.STONE, 10), false).isEmpty(), "Expected the interface to accept items");
                    helper.assertValueEqual(GameTestsCommon.getCore(helper, corePos).getStorage().getSlot(0).getCount(), 10L, "stored count");
                    helper.setBlock(min.offset(0, 1, 1), Blocks.AIR);
                })
                .thenWaitUntil(() -> GameTestsCommon.assertDormant(helper, corePos))
                .thenExecute(() -> {
                    // Holders of the old handler learn that it is gone.
                    helper.assertFalse(first[0].isPresent(), "Expected the cached handler to be invalidated");
                    helper.assertFalse(helper.getBlockEntity(wallPos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(),
                            "Expected no item handler on a dormant interface");
                })
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE_EMPTY)
    public void testUncolossalChestItemHandler(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value());
        IItemHandler handler = helper.getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElse(null);
        helper.assertTrue(handler != null, "Expected an item handler");
        helper.assertValueEqual(handler.getSlots(), 5, "slots");
        helper.assertTrue(handler.insertItem(4, new ItemStack(Items.STONE, 10), false).isEmpty(), "Expected the stone to fit");
        helper.assertValueEqual(((Container) helper.getBlockEntity(pos)).getItem(4).getCount(), 10, "stored count");
        helper.succeed();
    }

}
