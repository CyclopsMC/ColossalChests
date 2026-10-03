package org.cyclops.colossalchests2.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraft.world.SimpleContainer;
import org.cyclops.colossalchests2.Reference;
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

}
