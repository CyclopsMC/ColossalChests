package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestStorageCompression extends BootstrapTest {

    private static final ItemStack BLOCK = new ItemStack(Items.IRON_BLOCK);
    private static final ItemStack INGOT = new ItemStack(Items.IRON_INGOT);
    private static final ItemStack NUGGET = new ItemStack(Items.IRON_NUGGET);

    private CompressionFamilies families;
    private ChestStorage storage;

    @Before
    public void setUp() {
        families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        // 4 stacks per slot: 256 blocks.
        storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        storage.setCompression(() -> families);
    }

    @Test
    public void testAllFormsGoIntoOneSlotAsTheLargest() {
        assertEquals(5, storage.insert(NUGGET, 5, false));
        assertEquals(10, storage.insert(INGOT, 10, false));
        assertEquals(2, storage.insert(BLOCK, 2, false));
        DeepSlot slot = storage.getSlot(0);
        assertTrue(slot.matches(BLOCK));
        // 5 + 90 + 162 = 257 nuggets = 3 blocks and 14 nuggets.
        assertEquals(3, slot.getCount());
        assertEquals(14, slot.getRemainder());
        assertTrue(storage.getSlot(1).isEmpty());
    }

    @Test
    public void testExtractInEachForm() {
        storage.insert(BLOCK, 2, false);
        assertEquals(2, storage.getAvailable(0, BLOCK));
        assertEquals(18, storage.getAvailable(0, INGOT));
        assertEquals(162, storage.getAvailable(0, NUGGET));
        assertEquals(1, storage.extract(0, INGOT, 1, false));
        // 1 block and 8 ingots left, as 1 block with 72 nuggets of remainder.
        assertEquals(1, storage.getSlot(0).getCount());
        assertEquals(72, storage.getSlot(0).getRemainder());
        assertEquals(5, storage.extract(0, NUGGET, 5, false));
        assertEquals(1, storage.extract(0, BLOCK, 5, false));
        assertEquals(67, storage.getAvailable(0, NUGGET));
        assertEquals(0, storage.extract(0, BLOCK, 1, false));
    }

    @Test
    public void testMixedOrderGivesSameResult() {
        ChestStorage other = new ChestStorage(3, CapacityProfile.ofDepth(4));
        other.setCompression(() -> families);
        storage.insert(BLOCK, 1, false);
        storage.insert(NUGGET, 20, false);
        storage.insert(INGOT, 7, false);
        other.insert(INGOT, 7, false);
        other.insert(NUGGET, 20, false);
        other.insert(BLOCK, 1, false);
        assertEquals(storage.getSlot(0), other.getSlot(0));
    }

    @Test
    public void testCapacityCountsInBaseUnits() {
        assertEquals(256, storage.getCapacity(BLOCK));
        assertEquals(256 * 9, storage.getCapacity(INGOT));
        assertEquals(256 * 81, storage.getCapacity(NUGGET));
        storage.insert(0, BLOCK, 255, false);
        assertEquals(9, storage.insert(0, INGOT, 100, false));
        assertEquals(0, storage.insert(0, NUGGET, 1, false));
    }

    @Test
    public void testExtractionTypeFollowsChosenForm() {
        storage.insert(BLOCK, 1, false);
        assertTrue(ItemStack.isSameItem(BLOCK, storage.getExtractionType(0)));
        storage.setCompressionForm(0, Items.IRON_INGOT);
        assertTrue(ItemStack.isSameItem(INGOT, storage.getExtractionType(0)));
        assertEquals(9, storage.extract(0, 64, false));
        // A form outside the family falls back to the largest.
        storage.insert(BLOCK, 1, false);
        storage.setCompressionForm(0, Items.GOLD_INGOT);
        assertTrue(ItemStack.isSameItem(BLOCK, storage.getExtractionType(0)));
    }

    @Test
    public void testStorageWideExtractInAForm() {
        storage.insert(BLOCK, 3, false);
        assertEquals(20, storage.extract(INGOT, 20, false));
        // 27 - 20 = 7 ingots left: no whole block, 63 nuggets of remainder.
        assertEquals(0, storage.getSlot(0).getCount());
        assertEquals(63, storage.getSlot(0).getRemainder());
        assertEquals(7, storage.extract(INGOT, 20, false));
        assertTrue(storage.getSlot(0).isEmpty());
    }

    @Test
    public void testTurningCompressionOnConvertsSmallerForms() {
        ChestStorage plain = new ChestStorage(3, CapacityProfile.ofDepth(4));
        plain.insert(0, INGOT, 64, false);
        plain.insert(1, NUGGET, 9, false);
        plain.lockTo(2, NUGGET);
        plain.setCompression(() -> families);
        assertTrue(plain.getSlot(0).matches(BLOCK));
        assertEquals(7, plain.getSlot(0).getCount());
        assertEquals(9, plain.getSlot(0).getRemainder());
        assertEquals(0, plain.getSlot(1).getCount());
        assertEquals(9, plain.getSlot(1).getRemainder());
        assertTrue(plain.getSlot(2).isLocked() && plain.getSlot(2).matches(BLOCK));
    }

    @Test
    public void testLockingToASmallerFormReservesTheLargest() {
        assertTrue(storage.lockTo(0, NUGGET));
        assertTrue(storage.getSlot(0).matches(BLOCK));
        assertEquals(3, storage.insert(INGOT, 3, false));
        assertEquals(27, storage.getSlot(0).getRemainder());
    }

    @Test
    public void testRemovalRefusedWhileARemainderIsLeft() {
        storage.insert(INGOT, 10, false);
        assertEquals(List.of(0), ChestUpgrades.COMPRESSION.getRemovalProblems(storage));
        storage.extract(0, INGOT, 1, false);
        assertTrue(ChestUpgrades.COMPRESSION.getRemovalProblems(storage).isEmpty());
    }

    @Test
    public void testRemainderSurvivesSerialization() {
        storage.insert(NUGGET, 12, false);
        ChestStorage loaded = new ChestStorage(3, CapacityProfile.ofDepth(4));
        loaded.loadContents(storage.toContents());
        assertEquals(storage.getSlot(0), loaded.getSlot(0));
        assertEquals(12, loaded.getSlot(0).getRemainder());
    }

    @Test
    public void testOtherItemsAreNotCompressed() {
        assertEquals(10, storage.insert(new ItemStack(Items.GOLD_INGOT), 10, false));
        assertEquals(10, storage.getSlot(0).getCount());
        assertEquals(0, storage.getSlot(0).getRemainder());
    }

}
