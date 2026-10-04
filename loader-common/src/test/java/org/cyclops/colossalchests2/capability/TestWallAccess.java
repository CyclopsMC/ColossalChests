package org.cyclops.colossalchests2.capability;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.CompressionFamilies;
import org.cyclops.colossalchests2.storage.TestCompressionFamily;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestWallAccess extends BootstrapTest {

    private static final ItemStack STONE = new ItemStack(Items.STONE);
    private static final ItemStack DIRT = new ItemStack(Items.DIRT);

    private ChestStorage storage;

    @Before
    public void setUp() {
        storage = new ChestStorage(3, CapacityProfile.ofDepth(4));
    }

    private static WallAccess access(WallAccess.Mode mode, ItemStack... filter) {
        return new WallAccess(mode, List.of(filter), null, false);
    }

    @Test
    public void testModeMatrix() {
        for (WallAccess.Mode mode : WallAccess.Mode.values()) {
            for (boolean filtered : new boolean[]{false, true}) {
                WallAccess access = filtered ? access(mode, STONE) : access(mode);
                for (ItemStack type : new ItemStack[]{STONE, DIRT}) {
                    boolean passes = !filtered || type == STONE;
                    String name = mode + (filtered ? " filtered " : " ") + type.getItem();
                    assertEquals("insert " + name, mode != WallAccess.Mode.OUTPUT && passes, access.canInsert(storage, type));
                    assertEquals("extract " + name, mode != WallAccess.Mode.INPUT && passes, access.canExtract(storage, type));
                }
            }
        }
    }

    @Test
    public void testOpenAllowsEverything() {
        assertTrue(WallAccess.OPEN.canInsert(storage, STONE));
        assertTrue(WallAccess.OPEN.canExtract(storage, DIRT));
        assertFalse(WallAccess.OPEN.voidFull());
    }

    @Test
    public void testFilterComparesComponents() {
        ItemStack named = STONE.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Named"));
        WallAccess access = access(WallAccess.Mode.BOTH, named);
        assertTrue(access.allows(storage, named));
        assertFalse(access.allows(storage, STONE));
    }

    @Test
    public void testFilterIgnoresEmptyEntries() {
        assertTrue(access(WallAccess.Mode.BOTH, ItemStack.EMPTY).filter().isEmpty());
        assertFalse(access(WallAccess.Mode.BOTH, ItemStack.EMPTY, STONE).allows(storage, DIRT));
    }

    @Test
    public void testFilterIsACopy() {
        List<ItemStack> filter = new ArrayList<>(List.of(STONE.copy()));
        WallAccess access = new WallAccess(WallAccess.Mode.BOTH, filter, null, false);
        filter.set(0, DIRT.copy());
        assertTrue(access.allows(storage, STONE));
        assertFalse(access.allows(storage, DIRT));
    }

    @Test
    public void testFilterLetsCompressionFormsPass() {
        ItemStack ingot = new ItemStack(Items.IRON_INGOT);
        WallAccess access = access(WallAccess.Mode.BOTH, ingot);
        // Without compression, forms are just different items.
        assertFalse(access.allows(storage, new ItemStack(Items.IRON_BLOCK)));
        CompressionFamilies families = new CompressionFamilies();
        families.register(TestCompressionFamily.iron());
        storage.setCompression(() -> families);
        assertTrue(access.allows(storage, new ItemStack(Items.IRON_BLOCK)));
        assertTrue(access.allows(storage, new ItemStack(Items.IRON_NUGGET)));
        assertFalse(access.allows(storage, new ItemStack(Items.GOLD_INGOT)));
    }

    @Test
    public void testModeCycles() {
        assertEquals(WallAccess.Mode.INPUT, WallAccess.Mode.BOTH.next());
        assertEquals(WallAccess.Mode.OUTPUT, WallAccess.Mode.INPUT.next());
        assertEquals(WallAccess.Mode.BOTH, WallAccess.Mode.OUTPUT.next());
    }
}
