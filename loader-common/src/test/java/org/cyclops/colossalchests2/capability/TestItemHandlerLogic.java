package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.TestCompressionFamily;
import org.cyclops.colossalchests2.storage.CompressionFamilies;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Item handler behaviour, driven the way common consumers drive item handlers.
 * @author rubensworks
 */
public class TestItemHandlerLogic extends BootstrapTest {

    private ChestStorage storage;
    private ItemHandlerLogic handler;

    @Before
    public void setUp() {
        storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        handler = new ItemHandlerLogic(storage);
    }

    /**
     * Inserts like a hopper: one item at a time into the first slot that accepts it.
     * @return If the item was inserted.
     */
    private static boolean hopperInsertOne(ItemHandlerLogic handler, ItemStack type) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack single = type.copyWithCount(1);
            if (handler.insertItem(slot, single, true).isEmpty()) {
                handler.insertItem(slot, single, false);
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts like a hopper: one item from the first non-empty slot.
     * @return The extracted item.
     */
    private static ItemStack hopperExtractOne(ItemHandlerLogic handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (!handler.extractItem(slot, 1, true).isEmpty()) {
                return handler.extractItem(slot, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    @Test
    public void testHopperInsertLoopFillsSlotsInOrder() {
        int inserted = 0;
        while (hopperInsertOne(handler, new ItemStack(Items.STONE))) {
            inserted++;
        }
        assertEquals(3 * 256, inserted);
        assertEquals(256, handler.getStackInSlot(0).getCount());
        assertEquals(256, handler.getStackInSlot(2).getCount());
    }

    @Test
    public void testHopperInsertLoopRespectsTypes() {
        hopperInsertOne(handler, new ItemStack(Items.STONE));
        hopperInsertOne(handler, new ItemStack(Items.DIRT));
        hopperInsertOne(handler, new ItemStack(Items.STONE));
        assertEquals(2, handler.getStackInSlot(0).getCount());
        assertTrue(handler.getStackInSlot(1).is(Items.DIRT));
    }

    @Test
    public void testExtractAllLoop() {
        storage.insert(0, new ItemStack(Items.STONE), 200, false);
        storage.insert(2, new ItemStack(Items.DIRT), 30, false);
        int extracted = 0;
        while (!hopperExtractOne(handler).isEmpty()) {
            extracted++;
        }
        assertEquals(230, extracted);
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.getStackInSlot(2).isEmpty());
    }

    @Test
    public void testInsertSimulateVsExecute() {
        ItemStack stack = new ItemStack(Items.STONE, 64);
        assertTrue(handler.insertItem(0, stack, true).isEmpty());
        assertTrue(handler.getStackInSlot(0).isEmpty());
        assertTrue(handler.insertItem(0, stack, false).isEmpty());
        assertEquals(64, handler.getStackInSlot(0).getCount());
        assertEquals(64, stack.getCount());
    }

    @Test
    public void testInsertRemainder() {
        storage.insert(0, new ItemStack(Items.STONE), 250, false);
        ItemStack remainder = handler.insertItem(0, new ItemStack(Items.STONE, 64), false);
        assertEquals(58, remainder.getCount());
        assertTrue(remainder.is(Items.STONE));
        assertTrue(handler.insertItem(0, ItemStack.EMPTY, false).isEmpty());
    }

    @Test
    public void testExtractSimulateVsExecute() {
        storage.insert(0, new ItemStack(Items.STONE), 100, false);
        assertEquals(64, handler.extractItem(0, 64, true).getCount());
        assertEquals(100, storage.getSlot(0).getCount());
        assertEquals(64, handler.extractItem(0, 64, false).getCount());
        assertEquals(36, storage.getSlot(0).getCount());
    }

    @Test
    public void testExtractLimitedToOneStack() {
        storage.insert(0, new ItemStack(Items.STONE), 200, false);
        storage.insert(1, new ItemStack(Items.ENDER_PEARL), 50, false);
        assertEquals(64, handler.extractItem(0, 1000, false).getCount());
        assertEquals(16, handler.extractItem(1, 1000, false).getCount());
    }

    @Test
    public void testExtractNothing() {
        assertTrue(handler.extractItem(0, 10, false).isEmpty());
        storage.insert(0, new ItemStack(Items.STONE), 10, false);
        assertTrue(handler.extractItem(0, 0, false).isEmpty());
        storage.lockTo(1, new ItemStack(Items.DIRT));
        assertTrue(handler.extractItem(1, 10, false).isEmpty());
    }

    @Test
    public void testCountsAboveIntAreClamped() {
        ChestStorage big = new ChestStorage(1, CapacityProfile.ofDepth(1L << 40).withMaxItemsPerSlot(Long.MAX_VALUE));
        ItemHandlerLogic bigHandler = new ItemHandlerLogic(big);
        big.insert(0, new ItemStack(Items.STONE), Integer.MAX_VALUE + 5000L, false);
        assertEquals(Integer.MAX_VALUE, bigHandler.getStackInSlot(0).getCount());
        assertEquals(Integer.MAX_VALUE, bigHandler.getSlotLimit(0));
        assertEquals(64, bigHandler.extractItem(0, 64, false).getCount());
        assertEquals(Integer.MAX_VALUE + 5000L - 64, big.getSlot(0).getCount());
        assertEquals(Integer.MAX_VALUE, bigHandler.getStackInSlot(0).getCount());
    }

    @Test
    public void testGetStackInSlotIsACopy() {
        storage.insert(0, new ItemStack(Items.STONE), 10, false);
        handler.getStackInSlot(0).shrink(5);
        assertEquals(10, storage.getSlot(0).getCount());
    }

    @Test
    public void testSlotLimit() {
        assertEquals(256, handler.getSlotLimit(0));
        storage.insert(0, new ItemStack(Items.ENDER_PEARL), 1, false);
        assertEquals(64, handler.getSlotLimit(0));
    }

    @Test
    public void testIsItemValidHonoursLocks() {
        storage.lockTo(0, new ItemStack(Items.DIRT));
        assertFalse(handler.isItemValid(0, new ItemStack(Items.STONE)));
        assertTrue(handler.isItemValid(0, new ItemStack(Items.DIRT)));
        assertFalse(handler.insertItem(0, new ItemStack(Items.STONE), false).isEmpty());
        assertTrue(handler.isItemValid(1, new ItemStack(Items.STONE)));
    }

    @Test
    public void testAccessors() {
        assertSame(storage, handler.getStorage());
        assertSame(WallAccess.OPEN, handler.getAccess());
        assertEquals(3, handler.getSlots());
    }

    private ItemHandlerLogic handler(WallAccess.Mode mode, boolean voidFull, ItemStack... filter) {
        return new ItemHandlerLogic(storage, new WallAccess(mode, List.of(filter), voidFull));
    }

    @Test
    public void testOutputOnlyRejectsInserts() {
        ItemHandlerLogic output = handler(WallAccess.Mode.OUTPUT, false);
        ItemStack stone = new ItemStack(Items.STONE, 3);
        assertSame(stone, output.insertItem(0, stone, false));
        assertFalse(output.isItemValid(0, stone));
        assertTrue(storage.getSlot(0).isEmpty());
        storage.insert(0, stone, 3, false);
        assertEquals(3, output.extractItem(0, 64, false).getCount());
    }

    @Test
    public void testInputOnlyRejectsExtracts() {
        ItemHandlerLogic input = handler(WallAccess.Mode.INPUT, false);
        assertTrue(input.insertItem(0, new ItemStack(Items.STONE, 3), false).isEmpty());
        assertTrue(input.extractItem(0, 64, false).isEmpty());
        // Still visible, so consumers can see what is inside.
        assertEquals(3, input.getStackInSlot(0).getCount());
        assertEquals(3, storage.getSlot(0).getCount());
    }

    @Test
    public void testFilterLimitsBothDirections() {
        ItemHandlerLogic filtered = handler(WallAccess.Mode.BOTH, false, new ItemStack(Items.STONE));
        assertTrue(hopperInsertOne(filtered, new ItemStack(Items.STONE)));
        assertFalse(hopperInsertOne(filtered, new ItemStack(Items.DIRT)));
        storage.insert(1, new ItemStack(Items.DIRT), 2, false);
        assertTrue(filtered.extractItem(1, 64, false).isEmpty());
        assertEquals(1, filtered.extractItem(0, 64, false).getCount());
    }

    @Test
    public void testVoidFullThroughHandler() {
        ItemHandlerLogic voiding = handler(WallAccess.Mode.BOTH, true);
        ItemStack stone = new ItemStack(Items.STONE);
        storage.insert(0, stone, storage.getCapacity(stone), false);
        storage.insert(1, new ItemStack(Items.DIRT), 1, false);
        storage.insert(2, new ItemStack(Items.GRAVEL), 1, false);
        // A hopper finds a slot that takes the stone, which is destroyed.
        assertTrue(hopperInsertOne(voiding, stone));
        assertEquals(storage.getCapacity(stone), storage.getSlot(0).getCount());
        // A type the chest does not hold is not destroyed when there is no room for it.
        assertFalse(hopperInsertOne(voiding, new ItemStack(Items.SAND)));
    }

    @Test
    public void testFilterSetsTheExtractionForm() {
        CompressionFamilies families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        storage.setCompression(() -> families);
        storage.insert(new ItemStack(Items.IRON_BLOCK), 2, false);
        storage.insert(new ItemStack(Items.STONE), 5, false);
        ItemHandlerLogic nuggets = handler(WallAccess.Mode.BOTH, false, new ItemStack(Items.STONE), new ItemStack(Items.IRON_NUGGET));
        assertTrue(nuggets.getStackInSlot(0).is(Items.IRON_NUGGET));
        assertEquals(162, nuggets.getStackInSlot(0).getCount());
        ItemStack extracted = nuggets.extractItem(0, 64, false);
        assertTrue(extracted.is(Items.IRON_NUGGET));
        assertEquals(64, extracted.getCount());
        // Other slots and handlers without a form of the family keep the slot's own form.
        assertTrue(nuggets.getStackInSlot(1).is(Items.STONE));
        assertTrue(handler.getStackInSlot(0).is(Items.IRON_BLOCK));
    }

    @Test
    public void testFirstFormInTheFilterWins() {
        CompressionFamilies families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        storage.setCompression(() -> families);
        storage.insert(new ItemStack(Items.IRON_BLOCK), 1, false);
        ItemHandlerLogic ingots = handler(WallAccess.Mode.BOTH, false, new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_NUGGET));
        assertTrue(ingots.getStackInSlot(0).is(Items.IRON_INGOT));
    }

}
