package org.cyclops.colossalchests2.inventory;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestClickLogic extends BootstrapTest {

    private static final ItemStack STONE = new ItemStack(Items.STONE);
    private static final ItemStack PEARL = new ItemStack(Items.ENDER_PEARL);

    private ChestStorage storage;
    private FakeInventory inventory;

    @Before
    public void setUp() {
        storage = new ChestStorage(3, CapacityProfile.ofDepth(64));
        inventory = new FakeInventory(4, 64);
    }

    private ItemStack click(int slot, ChestClickAction action, ItemStack cursor) {
        return ChestClickLogic.click(storage, slot, action, cursor, inventory);
    }

    // Empty cursor

    @Test
    public void testLeftTakesOneMaxStack() {
        storage.insert(0, STONE, 1000, false);
        ItemStack cursor = click(0, ChestClickAction.TAKE_STACK, ItemStack.EMPTY);
        assertTrue(cursor.is(Items.STONE));
        assertEquals(64, cursor.getCount());
        assertEquals(936, storage.getSlot(0).getCount());
    }

    @Test
    public void testLeftTakesLessThanStackWhenFewer() {
        storage.insert(0, STONE, 10, false);
        assertEquals(10, click(0, ChestClickAction.TAKE_STACK, ItemStack.EMPTY).getCount());
        assertTrue(storage.getSlot(0).isEmpty());
    }

    @Test
    public void testLeftRespectsSmallMaxStack() {
        storage.insert(0, PEARL, 100, false);
        assertEquals(16, click(0, ChestClickAction.TAKE_STACK, ItemStack.EMPTY).getCount());
        assertEquals(84, storage.getSlot(0).getCount());
    }

    @Test
    public void testRightTakesHalfAMaxStack() {
        storage.insert(0, STONE, 1000, false);
        assertEquals(32, click(0, ChestClickAction.TAKE_HALF, ItemStack.EMPTY).getCount());
        assertEquals(968, storage.getSlot(0).getCount());
    }

    @Test
    public void testRightRoundsUpOnSmallCounts() {
        storage.insert(0, STONE, 5, false);
        assertEquals(3, click(0, ChestClickAction.TAKE_HALF, ItemStack.EMPTY).getCount());
        assertEquals(2, storage.getSlot(0).getCount());
    }

    @Test
    public void testTakeFromEmptySlot() {
        assertTrue(click(0, ChestClickAction.TAKE_STACK, ItemStack.EMPTY).isEmpty());
        assertTrue(click(0, ChestClickAction.TAKE_HALF, ItemStack.EMPTY).isEmpty());
    }

    @Test
    public void testShiftMovesOneStack() {
        storage.insert(0, STONE, 1000, false);
        ItemStack cursor = click(0, ChestClickAction.MOVE_STACK, ItemStack.EMPTY);
        assertTrue(cursor.isEmpty());
        assertEquals(List.of(64), inventory.counts());
        assertEquals(936, storage.getSlot(0).getCount());
    }

    @Test
    public void testCtrlMovesAsManyAsFit() {
        storage.insert(0, STONE, 1000, false);
        click(0, ChestClickAction.MOVE_ALL, ItemStack.EMPTY);
        assertEquals(List.of(64, 64, 64, 64), inventory.counts());
        assertEquals(1000 - 4 * 64, storage.getSlot(0).getCount());
    }

    @Test
    public void testCtrlEmptiesSmallSlot() {
        storage.insert(0, STONE, 100, false);
        click(0, ChestClickAction.MOVE_ALL, ItemStack.EMPTY);
        assertEquals(List.of(64, 36), inventory.counts());
        assertTrue(storage.getSlot(0).isEmpty());
    }

    @Test
    public void testMoveIntoFullInventoryKeepsItems() {
        inventory = new FakeInventory(0, 64);
        storage.insert(0, STONE, 100, false);
        click(0, ChestClickAction.MOVE_ALL, ItemStack.EMPTY);
        click(0, ChestClickAction.MOVE_STACK, ItemStack.EMPTY);
        assertEquals(100, storage.getSlot(0).getCount());
    }

    @Test
    public void testMoveKeepsCursor() {
        storage.insert(0, STONE, 100, false);
        ItemStack cursor = PEARL.copyWithCount(3);
        assertSame(cursor, click(0, ChestClickAction.MOVE_STACK, cursor));
    }

    // Item on the cursor

    @Test
    public void testLeftInsertsWholeCursor() {
        ItemStack cursor = click(1, ChestClickAction.TAKE_STACK, STONE.copyWithCount(40));
        assertTrue(cursor.isEmpty());
        assertEquals(40, storage.getSlot(1).getCount());
    }

    @Test
    public void testRightInsertsOneItem() {
        ItemStack cursor = click(1, ChestClickAction.TAKE_HALF, STONE.copyWithCount(40));
        assertEquals(39, cursor.getCount());
        assertEquals(1, storage.getSlot(1).getCount());
    }

    @Test
    public void testInsertOnOtherTypeGoesElsewhere() {
        storage.insert(0, PEARL, 5, false);
        ItemStack cursor = click(0, ChestClickAction.TAKE_STACK, STONE.copyWithCount(40));
        assertTrue(cursor.isEmpty());
        assertEquals(5, storage.getSlot(0).getCount());
        assertEquals(40, storage.getSlot(1).getCount());
    }

    @Test
    public void testInsertKeepsWhatDoesNotFit() {
        storage = new ChestStorage(1, CapacityProfile.ofDepth(1));
        storage.insert(0, STONE, 60, false);
        ItemStack cursor = click(0, ChestClickAction.TAKE_STACK, STONE.copyWithCount(10));
        assertEquals(6, cursor.getCount());
        assertEquals(64, storage.getSlot(0).getCount());
    }

    @Test
    public void testCursorInputIsNotModified() {
        ItemStack cursor = STONE.copyWithCount(40);
        click(1, ChestClickAction.TAKE_STACK, cursor);
        assertEquals(40, cursor.getCount());
    }

    /**
     * An inventory of a number of stone-only slots.
     */
    private static class FakeInventory implements ChestClickLogic.IPlayerInventory {

        private final List<Integer> slots = Lists.newArrayList();
        private final int slotCount;
        private final int maxStack;

        FakeInventory(int slotCount, int maxStack) {
            this.slotCount = slotCount;
            this.maxStack = maxStack;
        }

        @Override
        public ItemStack add(ItemStack stack) {
            int remaining = stack.getCount();
            for (int i = 0; i < slots.size() && remaining > 0; i++) {
                int added = Math.min(maxStack - slots.get(i), remaining);
                slots.set(i, slots.get(i) + added);
                remaining -= added;
            }
            while (remaining > 0 && slots.size() < slotCount) {
                int added = Math.min(maxStack, remaining);
                slots.add(added);
                remaining -= added;
            }
            return stack.copyWithCount(remaining);
        }

        List<Integer> counts() {
            return slots;
        }
    }

}
