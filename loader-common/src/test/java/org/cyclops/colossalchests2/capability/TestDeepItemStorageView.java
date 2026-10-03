package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestDeepItemStorageView extends BootstrapTest {

    @Test
    public void testExposesLongCounts() {
        ChestStorage storage = new ChestStorage(2, CapacityProfile.ofDepth(1L << 40).withMaxItemsPerSlot(Long.MAX_VALUE));
        DeepItemStorageView view = new DeepItemStorageView(storage);
        storage.insert(0, new ItemStack(Items.STONE), Integer.MAX_VALUE + 5000L, false);
        storage.lockTo(1, new ItemStack(Items.DIRT));

        assertEquals(2, view.getSlots());
        assertTrue(view.getType(0).is(Items.STONE));
        assertEquals(Integer.MAX_VALUE + 5000L, view.getCount(0));
        assertEquals((1L << 40) * 64, view.getCapacity(0));
        assertFalse(view.isLocked(0));
        assertTrue(view.getType(1).is(Items.DIRT));
        assertEquals(0, view.getCount(1));
        assertTrue(view.isLocked(1));
    }

}
