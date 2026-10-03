package org.cyclops.colossalchests2.storage;

import com.google.common.collect.Lists;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestStorage extends BootstrapTest {

    private static final ItemStack STONE = new ItemStack(Items.STONE);
    private static final ItemStack DIRT = new ItemStack(Items.DIRT);
    private static final ItemStack PEARL = new ItemStack(Items.ENDER_PEARL);
    private static final ItemStack PICKAXE = new ItemStack(Items.IRON_PICKAXE);

    private ChestStorage storage;
    private List<Integer> changes;

    @Before
    public void setUp() {
        storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
        changes = Lists.newArrayList();
        storage.addListener(changes::add);
    }

    // Construction and accessors

    @Test
    public void testConstruction() {
        assertEquals(3, storage.getSlotCount());
        assertEquals(CapacityProfile.ofDepth(4), storage.getProfile());
        assertSame(DeepSlot.EMPTY, storage.getSlot(0));
        assertEquals(0, storage.getState());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeSlotCount() {
        new ChestStorage(-1, CapacityProfile.ofDepth(4));
    }

    // Capacity

    @Test
    public void testCapacity() {
        assertEquals(256, storage.getCapacity(STONE));
        assertEquals(64, storage.getCapacity(PEARL));
        assertEquals(1, storage.getCapacity(PICKAXE));
        assertEquals(256, storage.getCapacity(0));
        storage.insert(1, PEARL, 1, false);
        assertEquals(64, storage.getCapacity(1));
    }

    @Test
    public void testNonStackableCapacityWithUpgrades() {
        storage.forceProfile(TestCapacityProfile.upgraded(4, 1, 2).build());
        assertEquals(8, storage.insert(0, PICKAXE, 100, false));
        assertEquals(8, storage.getSlot(0).getCount());
    }

    @Test
    public void testNonStackablesRejectedWhenDisabled() {
        storage.forceProfile(storage.getProfile().withAcceptNonStackables(false));
        assertFalse(storage.canAccept(0, PICKAXE));
        assertEquals(0, storage.insert(PICKAXE, 1, false));
    }

    // Insert

    @Test
    public void testInsertIntoEmpty() {
        assertEquals(100, storage.insert(0, STONE, 100, false));
        assertTrue(storage.getSlot(0).matches(STONE));
        assertEquals(100, storage.getSlot(0).getCount());
    }

    @Test
    public void testInsertIntoMatching() {
        storage.insert(0, STONE, 100, false);
        assertEquals(50, storage.insert(0, STONE, 50, false));
        assertEquals(150, storage.getSlot(0).getCount());
    }

    @Test
    public void testInsertIntoFull() {
        storage.insert(0, STONE, 256, false);
        assertEquals(0, storage.insert(0, STONE, 1, false));
    }

    @Test
    public void testInsertPartiallyFits() {
        storage.insert(0, STONE, 200, false);
        assertEquals(56, storage.insert(0, STONE, 100, false));
        assertEquals(256, storage.getSlot(0).getCount());
    }

    @Test
    public void testInsertIntoMismatched() {
        storage.insert(0, STONE, 1, false);
        assertFalse(storage.canAccept(0, DIRT));
        assertEquals(0, storage.insert(0, DIRT, 10, false));
    }

    @Test
    public void testInsertIntoLockedOtherType() {
        storage.lockTo(0, STONE);
        assertEquals(0, storage.insert(0, DIRT, 10, false));
        assertEquals(10, storage.insert(0, STONE, 10, false));
    }

    @Test
    public void testInsertDistinctComponentsAreDistinctTypes() {
        ItemStack named = STONE.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        storage.insert(0, STONE, 1, false);
        assertEquals(0, storage.insert(0, named, 1, false));
        assertEquals(1, storage.insert(named, 1, false));
        assertTrue(storage.getSlot(1).matches(named));
    }

    @Test
    public void testInsertEmptyOrNonPositive() {
        assertEquals(0, storage.insert(0, ItemStack.EMPTY, 1, false));
        assertEquals(0, storage.insert(0, STONE, 0, false));
        assertEquals(0, storage.insert(0, STONE, -5, false));
    }

    @Test
    public void testInsertSimulate() {
        assertEquals(100, storage.insert(0, STONE, 100, true));
        assertSame(DeepSlot.EMPTY, storage.getSlot(0));
        assertEquals(0, storage.getState());
    }

    @Test
    public void testInsertAnywherePrefersMatching() {
        storage.insert(2, STONE, 10, false);
        assertEquals(20, storage.insert(STONE, 20, false));
        assertEquals(30, storage.getSlot(2).getCount());
        assertTrue(storage.getSlot(0).isEmpty());
    }

    @Test
    public void testInsertAnywhereSpillsIntoFreeSlots() {
        assertEquals(600, storage.insert(STONE, 600, false));
        assertEquals(256, storage.getSlot(0).getCount());
        assertEquals(256, storage.getSlot(1).getCount());
        assertEquals(88, storage.getSlot(2).getCount());
    }

    @Test
    public void testInsertAnywhereSimulateMatchesExecute() {
        storage.insert(1, DIRT, 1, false);
        long simulated = storage.insert(STONE, 1000, true);
        assertEquals(512, simulated);
        assertEquals(simulated, storage.insert(STONE, 1000, false));
    }

    @Test
    public void testInsertAnywhereSkipsLockedSlots() {
        storage.lockTo(0, DIRT);
        storage.insert(STONE, 1, false);
        assertTrue(storage.getSlot(1).matches(STONE));
    }

    @Test
    public void testCanFit() {
        assertTrue(storage.canFit(STONE));
        storage.insert(STONE, 768, false);
        assertFalse(storage.canFit(STONE));
        assertFalse(storage.canFit(DIRT));
    }

    // Extract

    @Test
    public void testExtractPartial() {
        storage.insert(0, STONE, 100, false);
        assertEquals(30, storage.extract(0, 30, false));
        assertEquals(70, storage.getSlot(0).getCount());
    }

    @Test
    public void testExtractFull() {
        storage.insert(0, STONE, 100, false);
        assertEquals(100, storage.extract(0, 100, false));
        assertSame(DeepSlot.EMPTY, storage.getSlot(0));
    }

    @Test
    public void testExtractMoreThanPresent() {
        storage.insert(0, STONE, 100, false);
        assertEquals(100, storage.extract(0, 1000, false));
        assertEquals(0, storage.extract(0, 1, false));
        assertEquals(0, storage.extract(1, 1, false));
        assertEquals(0, storage.extract(0, -1, false));
    }

    @Test
    public void testExtractSimulate() {
        storage.insert(0, STONE, 100, false);
        int state = storage.getState();
        assertEquals(40, storage.extract(0, 40, true));
        assertEquals(100, storage.getSlot(0).getCount());
        assertEquals(state, storage.getState());
    }

    @Test
    public void testExtractLockedKeepsType() {
        storage.insert(0, STONE, 10, false);
        storage.setLocked(0, true);
        storage.extract(0, 10, false);
        assertTrue(storage.getSlot(0).matches(STONE));
        assertEquals(0, storage.getSlot(0).getCount());
    }

    @Test
    public void testExtractFromExtractOnly() {
        storage.insert(0, STONE, 256, false);
        storage.forceProfile(CapacityProfile.ofDepth(2));
        assertTrue(storage.isExtractOnly(0));
        assertEquals(0, storage.insert(0, STONE, 1, false));
        assertEquals(100, storage.extract(0, 100, false));
        assertTrue(storage.isExtractOnly(0));
        assertEquals(28, storage.extract(0, 28, false));
        assertFalse(storage.isExtractOnly(0));
        assertEquals(0, storage.insert(0, STONE, 1, false));
        storage.extract(0, 1, false);
        assertEquals(1, storage.insert(0, STONE, 1, false));
    }

    @Test
    public void testExtractByType() {
        storage.insert(0, STONE, 10, false);
        storage.insert(1, DIRT, 10, false);
        storage.insert(2, STONE, 10, false);
        assertEquals(15, storage.extract(STONE, 15, false));
        assertTrue(storage.getSlot(0).isEmpty());
        assertEquals(5, storage.getSlot(2).getCount());
        assertEquals(10, storage.getSlot(1).getCount());
    }

    // Extract-only

    @Test
    public void testIsExtractOnly() {
        assertFalse(storage.isExtractOnly(0));
        storage.insert(0, STONE, 256, false);
        assertFalse(storage.isExtractOnly(0));
        storage.forceProfile(CapacityProfile.ofDepth(1));
        assertTrue(storage.isExtractOnly(0));
    }

    // Locks

    @Test
    public void testSetLocked() {
        assertTrue(storage.setLocked(0, false));
        assertFalse(storage.setLocked(0, true));
        storage.insert(0, STONE, 1, false);
        assertTrue(storage.setLocked(0, true));
        assertTrue(storage.getSlot(0).isLocked());
        assertTrue(storage.setLocked(0, false));
        assertFalse(storage.getSlot(0).isLocked());
    }

    @Test
    public void testLockTo() {
        assertTrue(storage.lockTo(0, STONE));
        assertTrue(storage.getSlot(0).isLocked());
        assertTrue(storage.getSlot(0).matches(STONE));
        assertTrue(storage.lockTo(0, DIRT));
        assertTrue(storage.getSlot(0).matches(DIRT));
        assertFalse(storage.lockTo(1, ItemStack.EMPTY));
        storage.insert(1, STONE, 5, false);
        assertFalse(storage.lockTo(1, DIRT));
        assertTrue(storage.lockTo(1, STONE));
        assertEquals(5, storage.getSlot(1).getCount());
    }

    @Test
    public void testLockAllFilledAndClearLocks() {
        storage.insert(0, STONE, 5, false);
        storage.insert(2, DIRT, 5, false);
        assertEquals(2, storage.lockAllFilled());
        assertEquals(0, storage.lockAllFilled());
        assertTrue(storage.getSlot(0).isLocked());
        assertFalse(storage.getSlot(1).isLocked());
        storage.extract(2, 5, false);
        storage.clearLocks();
        assertFalse(storage.getSlot(0).isLocked());
        assertSame(DeepSlot.EMPTY, storage.getSlot(2));
    }

    @Test
    public void testSetCompressionForm() {
        storage.setCompressionForm(0, Items.IRON_INGOT);
        assertSame(DeepSlot.EMPTY, storage.getSlot(0));
        storage.insert(0, STONE, 5, false);
        storage.setCompressionForm(0, Items.IRON_INGOT);
        assertEquals(Optional.of(Items.IRON_INGOT), storage.getSlot(0).getCompressionForm());
        int state = storage.getState();
        storage.setCompressionForm(0, Items.IRON_INGOT);
        assertEquals(state, storage.getState());
        storage.setCompressionForm(0, null);
        assertEquals(Optional.empty(), storage.getSlot(0).getCompressionForm());
    }

    // Resize

    @Test
    public void testValidateSlotCount() {
        storage.insert(2, STONE, 1, false);
        storage.lockTo(1, DIRT);
        assertTrue(storage.validateSlotCount(5).isOk());
        assertTrue(storage.validateSlotCount(3).isOk());
        assertEquals(List.of(2), storage.validateSlotCount(1).offendingSlots());
    }

    @Test
    public void testSetSlotCountGrow() {
        storage.insert(0, STONE, 1, false);
        assertTrue(storage.setSlotCount(5).isOk());
        assertEquals(5, storage.getSlotCount());
        assertSame(DeepSlot.EMPTY, storage.getSlot(4));
        assertEquals(1, storage.getSlot(0).getCount());
        assertTrue(changes.contains(-1));
    }

    @Test
    public void testSetSlotCountShrinkRefused() {
        storage.insert(2, STONE, 1, false);
        int state = storage.getState();
        assertFalse(storage.setSlotCount(2).isOk());
        assertEquals(3, storage.getSlotCount());
        assertEquals(state, storage.getState());
    }

    @Test
    public void testSetSlotCountShrinkAllowed() {
        storage.insert(0, STONE, 1, false);
        assertTrue(storage.setSlotCount(1).isOk());
        assertEquals(1, storage.getSlotCount());
    }

    @Test
    public void testValidateProfileBothDirections() {
        storage.insert(0, STONE, 200, false);
        storage.insert(1, PEARL, 30, false);
        assertTrue(storage.validateProfile(CapacityProfile.ofDepth(64)).isOk());
        assertEquals(List.of(0), storage.validateProfile(CapacityProfile.ofDepth(3)).offendingSlots());
        assertEquals(List.of(0, 1), storage.validateProfile(CapacityProfile.ofDepth(1)).offendingSlots());
    }

    @Test
    public void testSetProfile() {
        storage.insert(0, STONE, 200, false);
        assertFalse(storage.setProfile(CapacityProfile.ofDepth(3)).isOk());
        assertEquals(CapacityProfile.ofDepth(4), storage.getProfile());
        assertTrue(storage.setProfile(CapacityProfile.ofDepth(8)).isOk());
        assertEquals(CapacityProfile.ofDepth(8), storage.getProfile());
    }

    @Test
    public void testForceProfile() {
        storage.insert(0, STONE, 200, false);
        ResizeResult result = storage.forceProfile(CapacityProfile.ofDepth(1));
        assertEquals(List.of(0), result.offendingSlots());
        assertEquals(CapacityProfile.ofDepth(1), storage.getProfile());
        assertEquals(200, storage.getSlot(0).getCount());
    }

    // Listeners, dirty tracking and state

    @Test
    public void testListenersAndState() {
        storage.insert(1, STONE, 1, false);
        storage.insert(1, STONE, 1, true);
        assertEquals(List.of(1), changes);
        assertEquals(1, storage.getState());
        storage.forceProfile(CapacityProfile.ofDepth(8));
        assertEquals(List.of(1, -1), changes);
        assertEquals(2, storage.getState());
    }

    @Test
    public void testRemoveListener() {
        storage.removeListener(changes::add);
        List<Integer> other = Lists.newArrayList();
        java.util.function.IntConsumer listener = other::add;
        storage.addListener(listener);
        storage.removeListener(listener);
        storage.insert(0, STONE, 1, false);
        assertTrue(other.isEmpty());
    }

    @Test
    public void testStateUnchangedWithoutChange() {
        storage.insert(0, STONE, 256, false);
        int state = storage.getState();
        storage.insert(0, STONE, 1, false);
        storage.setLocked(1, false);
        storage.forceProfile(storage.getProfile());
        assertEquals(state, storage.getState());
    }

    @Test
    public void testDirtySlots() {
        assertFalse(storage.hasDirtySlots());
        storage.insert(2, STONE, 1, false);
        storage.insert(0, STONE, 1, false);
        assertTrue(storage.hasDirtySlots());
        assertArrayEquals(new int[]{0, 2}, storage.drainDirtySlots());
        assertFalse(storage.hasDirtySlots());
        assertArrayEquals(new int[0], storage.drainDirtySlots());
        storage.markAllDirty();
        assertArrayEquals(new int[]{0, 1, 2}, storage.drainDirtySlots());
    }

    // Snapshots

    @Test
    public void testSnapshotRestore() {
        storage.insert(0, STONE, 10, false);
        DeepSlot[] snapshot = storage.snapshotSlots();
        storage.insert(0, STONE, 5, false);
        storage.insert(1, DIRT, 5, false);
        storage.drainDirtySlots();
        changes.clear();
        storage.restoreSlots(snapshot);
        assertEquals(10, storage.getSlot(0).getCount());
        assertSame(DeepSlot.EMPTY, storage.getSlot(1));
        assertArrayEquals(new int[]{0, 1}, storage.drainDirtySlots());
        assertEquals(List.of(0, 1), changes);
    }

    @Test
    public void testSnapshotIsIndependent() {
        DeepSlot[] snapshot = storage.snapshotSlots();
        storage.insert(0, STONE, 10, false);
        assertSame(DeepSlot.EMPTY, snapshot[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRestoreWrongSize() {
        storage.restoreSlots(new DeepSlot[1]);
    }

    // Serialization

    @Test
    public void testContentsAreSparse() {
        storage.insert(1, STONE, 5, false);
        ChestStorage.Contents contents = storage.toContents();
        assertEquals(3, contents.slotCount());
        assertEquals(1, contents.entries().size());
        assertEquals(1, contents.entries().get(0).slot());
    }

    @Test
    public void testSerializationRoundTrip() {
        ChestStorage big = new ChestStorage(81, TestCapacityProfile.upgraded(262144, 8, 0).maxItemsPerSlot(Long.MAX_VALUE).build());
        ItemStack named = STONE.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        ItemStack damaged = PICKAXE.copy();
        damaged.setDamageValue(7);
        big.insert(0, STONE, Integer.MAX_VALUE, false);
        big.insert(5, DIRT, Integer.MAX_VALUE + 1000L, false);
        big.insert(40, named, 3, false);
        big.insert(41, damaged, 1, false);
        big.lockTo(80, PEARL);
        big.setCompressionForm(5, Items.IRON_NUGGET);
        // Lower the technical cap: slot 5 is now above it and extract-only.
        big.forceProfile(big.getProfile().withMaxItemsPerSlot(Integer.MAX_VALUE));
        assertTrue(big.isExtractOnly(5));

        Tag tag = ChestStorage.CODEC.encodeStart(NbtOps.INSTANCE, big.toContents()).getOrThrow();
        ChestStorage loaded = new ChestStorage(0, big.getProfile());
        loaded.loadContents(ChestStorage.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow());

        assertEquals(81, loaded.getSlotCount());
        for (int slot = 0; slot < 81; slot++) {
            assertEquals("slot " + slot, big.getSlot(slot), loaded.getSlot(slot));
        }
        assertEquals(Integer.MAX_VALUE, loaded.getSlot(0).getCount());
        assertEquals(Integer.MAX_VALUE + 1000L, loaded.getSlot(5).getCount());
        assertTrue(loaded.isExtractOnly(5));
        assertFalse(loaded.isExtractOnly(0));
        assertTrue(loaded.getSlot(80).isLocked());
        assertEquals(Optional.of(Items.IRON_NUGGET), loaded.getSlot(5).getCompressionForm());
    }

    @Test
    public void testSerializationJson() {
        storage.insert(2, STONE, 42, false);
        var json = ChestStorage.CODEC.encodeStart(JsonOps.INSTANCE, storage.toContents()).getOrThrow();
        ChestStorage loaded = new ChestStorage(0, storage.getProfile());
        loaded.loadContents(ChestStorage.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        assertEquals(storage.getSlot(2), loaded.getSlot(2));
    }

    @Test
    public void testUnknownCompressionFormLoadsAsDefault() {
        var json = com.google.gson.JsonParser.parseString(
                "{\"slot_count\": 1, \"slots\": [{\"slot\": 0, \"item\": {\"id\": \"minecraft:stone\"}, \"count\": 3, \"form\": \"othermod:missing\"}]}");
        storage.loadContents(ChestStorage.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        assertEquals(3, storage.getSlot(0).getCount());
        assertEquals(Optional.empty(), storage.getSlot(0).getCompressionForm());
    }

    @Test
    public void testUndecodableEntrySkipped() {
        var json = com.google.gson.JsonParser.parseString(
                "{\"slot_count\": 2, \"slots\": [{\"slot\": 0, \"item\": {\"id\": \"othermod:missing\"}, \"count\": 3},"
                        + " {\"slot\": 1, \"item\": {\"id\": \"minecraft:stone\"}, \"count\": 4}]}");
        storage.loadContents(ChestStorage.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        assertSame(DeepSlot.EMPTY, storage.getSlot(0));
        assertEquals(4, storage.getSlot(1).getCount());
    }

    @Test
    public void testLoadContentsNeverDropsEntries() {
        ChestStorage.Contents contents = new ChestStorage.Contents(2, List.of(
                new ChestStorage.Contents.Entry(4, STONE, 7, false, Optional.empty())));
        storage.loadContents(contents);
        assertEquals(5, storage.getSlotCount());
        assertEquals(7, storage.getSlot(4).getCount());
    }

    @Test
    public void testLoadContentsMarksDirtyAndNotifies() {
        storage.loadContents(new ChestStorage.Contents(2, List.of()));
        assertArrayEquals(new int[]{0, 1}, storage.drainDirtySlots());
        assertEquals(List.of(-1), changes);
    }

}
