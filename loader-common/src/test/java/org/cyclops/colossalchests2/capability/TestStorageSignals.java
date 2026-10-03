package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestStorageSignals extends BootstrapTest {

    @Test
    public void testEmpty() {
        assertEquals(0, StorageSignals.getComparatorSignal(new ChestStorage(3, CapacityProfile.ofDepth(4))));
        assertEquals(0, StorageSignals.getComparatorSignal(new ChestStorage(0, CapacityProfile.ofDepth(4))));
    }

    @Test
    public void testLockedEmptySlotIsEmpty() {
        ChestStorage storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        storage.lockTo(0, new ItemStack(Items.STONE));
        assertEquals(0, StorageSignals.getComparatorSignal(storage));
    }

    @Test
    public void testSingleItemGivesOne() {
        ChestStorage storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 1, false);
        assertEquals(1, StorageSignals.getComparatorSignal(storage));
    }

    @Test
    public void testUsesDeepCapacity() {
        // A slot with 64 items out of 256 is a quarter full, not full as a max-stack-size based formula would say.
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 64, false);
        assertEquals(4, StorageSignals.getComparatorSignal(storage));
    }

    @Test
    public void testFull() {
        ChestStorage storage = new ChestStorage(2, CapacityProfile.ofDepth(4));
        storage.insert(new ItemStack(Items.STONE), 512, false);
        assertEquals(15, StorageSignals.getComparatorSignal(storage));
    }

    @Test
    public void testOverCapacityCountsAsFull() {
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 256, false);
        storage.forceProfile(CapacityProfile.ofDepth(1));
        assertEquals(15, StorageSignals.getComparatorSignal(storage));
    }

    @Test
    public void testZeroCapacityCountsAsFull() {
        ChestStorage storage = new ChestStorage(1, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.IRON_PICKAXE), 1, false);
        storage.forceProfile(CapacityProfile.ofDepth(4).withAcceptNonStackables(false));
        assertEquals(15, StorageSignals.getComparatorSignal(storage));
    }

    @Test
    public void testHalf() {
        ChestStorage storage = new ChestStorage(2, CapacityProfile.ofDepth(4));
        storage.insert(0, new ItemStack(Items.STONE), 256, false);
        assertEquals(8, StorageSignals.getComparatorSignal(storage));
    }

}
