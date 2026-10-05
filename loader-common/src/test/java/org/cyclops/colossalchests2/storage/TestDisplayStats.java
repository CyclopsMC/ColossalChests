package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestDisplayStats extends BootstrapTest {

    private static final ItemStack STONE = new ItemStack(Items.STONE);

    private ChestStorage storage;

    @Before
    public void setUp() {
        // 4 stacks per slot.
        storage = new ChestStorage(4, CapacityProfile.ofDepth(4));
    }

    @Test
    public void testEmptyType() {
        assertEquals(DisplayStats.EMPTY, DisplayStats.of(storage, ItemStack.EMPTY));
        assertEquals(0, DisplayStats.EMPTY.getFillLevel(), 0);
    }

    @Test
    public void testNotStored() {
        DisplayStats stats = DisplayStats.of(storage, STONE);
        assertEquals(0, stats.count());
        assertEquals(0, stats.capacity());
        assertEquals(0, stats.getFillLevel(), 0);
    }

    @Test
    public void testCountsAcrossSlots() {
        storage.insert(0, STONE, 256, false);
        storage.insert(2, STONE, 64, false);
        storage.insert(1, new ItemStack(Items.DIRT), 10, false);
        DisplayStats stats = DisplayStats.of(storage, STONE);
        assertEquals(320, stats.count());
        assertEquals(512, stats.capacity());
        assertEquals(0.625F, stats.getFillLevel(), 0.0001F);
        assertFalse(stats.locked());
        assertFalse(stats.voided());
        assertFalse(stats.compressed());
    }

    @Test
    public void testLockedAndVoided() {
        storage.lockTo(1, STONE);
        assertTrue(DisplayStats.of(storage, STONE).locked());
        assertEquals(0, DisplayStats.of(storage, STONE).count());
        storage.insert(1, STONE, 5, false);
        storage.setVoiding(1, true);
        DisplayStats stats = DisplayStats.of(storage, STONE);
        assertTrue(stats.voided());
        assertEquals(5, stats.count());
    }

    @Test
    public void testCompressedFormsCount() {
        CompressionFamilies families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        storage.setCompression(() -> families);
        storage.insert(new ItemStack(Items.IRON_BLOCK), 2, false);
        storage.insert(new ItemStack(Items.IRON_NUGGET), 5, false);
        DisplayStats ingots = DisplayStats.of(storage, new ItemStack(Items.IRON_INGOT));
        assertTrue(ingots.compressed());
        assertEquals(18, ingots.count());
        assertEquals(167, DisplayStats.of(storage, new ItemStack(Items.IRON_NUGGET)).count());
    }

    @Test
    public void testTagRoundTrip() {
        DisplayStats stats = new DisplayStats(123456789012L, 99, true, false, true);
        assertEquals(stats, DisplayStats.fromTag(stats.toTag()));
    }
}
