package org.cyclops.colossalchests2.storage;

import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestStorageVoid extends BootstrapTest {

    private static final ItemStack STONE = new ItemStack(Items.STONE);
    private static final ItemStack DIRT = new ItemStack(Items.DIRT);

    private ChestStorage storage;

    @Before
    public void setUp() {
        // 4 stacks per slot.
        storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
    }

    @Test
    public void testOnlySlotsWithATypeCanBeMarked() {
        assertFalse(storage.setVoiding(0, true));
        assertTrue(storage.setVoiding(0, false));
        storage.insert(0, STONE, 1, false);
        assertTrue(storage.setVoiding(0, true));
        assertTrue(storage.getSlot(0).isVoiding());
    }

    @Test
    public void testMarkIsLostWhenAnUnlockedSlotEmpties() {
        storage.insert(0, STONE, 1, false);
        storage.setVoiding(0, true);
        storage.extract(0, 1, false);
        assertEquals(DeepSlot.EMPTY, storage.getSlot(0));
    }

    @Test
    public void testMarkStaysOnAReservedSlot() {
        storage.lockTo(0, STONE);
        assertTrue(storage.setVoiding(0, true));
        assertTrue(storage.getSlot(0).isVoiding());
        assertEquals(0, storage.getSlot(0).getCount());
    }

    @Test
    public void testVoidAllAndClear() {
        storage.insert(0, STONE, 1, false);
        storage.insert(1, DIRT, 1, false);
        assertEquals(2, storage.voidAllFilled());
        assertFalse(storage.getSlot(2).isVoiding());
        storage.clearVoids();
        assertFalse(storage.getSlot(0).isVoiding() || storage.getSlot(1).isVoiding());
    }

    @Test
    public void testAutomatedInsertVoidsOverflowOfMarkedType() {
        storage.insert(0, STONE, 256, false);
        storage.setVoiding(0, true);
        // The other slots are free, but voided overflow never takes new slots: it is destroyed.
        assertEquals(10, storage.insertAutomated(STONE, 10, true));
        assertEquals(10, storage.insertAutomated(STONE, 10, false));
        assertEquals(256, storage.getSlot(0).getCount());
        assertTrue(storage.getSlot(1).isEmpty());
        // A player insert is never voided, and may use a free slot.
        assertEquals(10, storage.insert(STONE, 10, false));
        assertEquals(10, storage.getSlot(1).getCount());
    }

    @Test
    public void testAutomatedInsertFillsBeforeVoiding() {
        storage.insert(0, STONE, 250, false);
        storage.setVoiding(0, true);
        assertEquals(10, storage.insertAutomated(STONE, 10, false));
        assertEquals(256, storage.getSlot(0).getCount());
    }

    @Test
    public void testUnmarkedTypesAreNotVoided() {
        storage.insert(0, STONE, 256, false);
        storage.setVoiding(0, true);
        storage.insert(1, DIRT, 256, false);
        storage.insert(2, Items.COBBLESTONE.getDefaultInstance(), 256, false);
        assertEquals(0, storage.insertAutomated(DIRT, 10, false));
        assertEquals(0, storage.insertAutomated(new ItemStack(Items.ANDESITE), 10, false));
    }

    @Test
    public void testSlotInsertVoidsOnlyWhenNothingElseFits() {
        storage.insert(0, STONE, 256, false);
        storage.setVoiding(0, true);
        storage.lockTo(1, STONE);
        // Slot 1 can still take stone, so automation walking the slots must not lose it at slot 0.
        assertEquals(0, storage.insertAutomated(0, STONE, 10, false));
        assertEquals(10, storage.insertAutomated(1, STONE, 10, false));
        storage.insert(1, STONE, 246, false);
        assertEquals(10, storage.insertAutomated(0, STONE, 10, false));
        // Only the voiding slot voids.
        assertEquals(0, storage.insertAutomated(1, STONE, 10, false));
    }

    @Test
    public void testMarksSurviveSerialization() {
        storage.insert(0, STONE, 3, false);
        storage.setVoiding(0, true);
        ChestStorage.Contents contents = ChestStorage.CODEC.parse(NbtOps.INSTANCE,
                ChestStorage.CODEC.encodeStart(NbtOps.INSTANCE, storage.toContents()).getOrThrow()).getOrThrow();
        ChestStorage loaded = new ChestStorage(3, CapacityProfile.ofDepth(4));
        loaded.loadContents(contents);
        assertTrue(loaded.getSlot(0).isVoiding());
        assertEquals(3, loaded.getSlot(0).getCount());
    }

    @Test
    public void testVoidFullDestroysOverflowOfHeldTypes() {
        storage.insert(0, STONE, 250, false);
        assertEquals(20, storage.insertAutomated(STONE, 20, false, true));
        assertEquals(256, storage.getSlot(0).getCount());
        // Overflow never takes new slots.
        assertTrue(storage.getSlot(1).isEmpty());
        assertTrue(storage.getSlot(2).isEmpty());
    }

    @Test
    public void testVoidFullStoresNewTypes() {
        assertEquals(10, storage.insertAutomated(DIRT, 10, false, true));
        assertEquals(10, storage.getSlot(0).getCount());
    }

    @Test
    public void testVoidFullRejectsNewTypesWithoutRoom() {
        storage.insert(0, STONE, 1, false);
        storage.insert(1, new ItemStack(Items.GRAVEL), 1, false);
        storage.insert(2, new ItemStack(Items.SAND), 1, false);
        assertEquals(0, storage.insertAutomated(DIRT, 5, false, true));
    }

    @Test
    public void testVoidFullIntoSlotOnlyWhenNoHoldingSlotHasRoom() {
        storage.insert(0, STONE, 256, false);
        storage.insert(1, STONE, 10, false);
        // Slot 1 still has room, so a hopper walking the slots gets there.
        assertEquals(0, storage.insertAutomated(0, STONE, 5, false, true));
        storage.insert(1, STONE, 246, false);
        assertEquals(5, storage.insertAutomated(0, STONE, 5, false, true));
        assertEquals(256, storage.getSlot(1).getCount());
    }

    @Test
    public void testWithoutVoidFullNothingIsVoided() {
        storage.insert(0, STONE, 256, false);
        storage.insert(1, STONE, 256, false);
        storage.insert(2, STONE, 256, false);
        assertEquals(0, storage.insertAutomated(STONE, 5, false, false));
        assertEquals(0, storage.insertAutomated(0, STONE, 5, false, false));
    }

}
