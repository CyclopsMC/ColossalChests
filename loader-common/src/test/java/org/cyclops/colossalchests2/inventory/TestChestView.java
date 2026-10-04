package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestView extends BootstrapTest {

    // Names by item id, as the server may not know translations.
    private static final Function<ItemStack, String> NAMES = stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

    private static ChestStorage storage(Object... itemsAndCounts) {
        ChestStorage storage = new ChestStorage(81, CapacityProfile.ofDepth(1000));
        for (int i = 0; i < itemsAndCounts.length; i += 3) {
            storage.insert((int) itemsAndCounts[i], new ItemStack((Item) itemsAndCounts[i + 1]), (int) itemsAndCounts[i + 2], false);
        }
        return storage;
    }

    private static int[] head(int[] view, int length) {
        return Arrays.copyOf(view, length);
    }

    @Test
    public void testNoSortKeepsSlotOrderWithEmptySlots() {
        ChestStorage storage = storage(3, Items.STONE, 5, 0, Items.DIRT, 1);
        int[] view = ChestView.compute(storage, "", ChestSortMode.NONE, NAMES);
        assertEquals(81, view.length);
        for (int i = 0; i < 81; i++) {
            assertEquals(i, view[i]);
        }
    }

    @Test
    public void testSortByName() {
        ChestStorage storage = storage(5, Items.STONE, 1, 2, Items.ANDESITE, 1, 9, Items.DIRT, 1);
        int[] view = ChestView.compute(storage, "", ChestSortMode.NAME, NAMES);
        assertArrayEquals(new int[]{2, 9, 5}, head(view, 3));
        assertEquals(81, view.length);
        // Empty slots follow in slot order.
        assertArrayEquals(new int[]{0, 1, 3}, Arrays.copyOfRange(view, 3, 6));
    }

    @Test
    public void testSortByCount() {
        ChestStorage storage = storage(5, Items.STONE, 10, 2, Items.ANDESITE, 500, 9, Items.DIRT, 10, 40, Items.SAND, 64);
        int[] view = ChestView.compute(storage, "", ChestSortMode.COUNT, NAMES);
        // Equal counts keep slot order.
        assertArrayEquals(new int[]{2, 40, 5, 9}, head(view, 4));
    }

    @Test
    public void testSortByMod() {
        // All vanilla, so this sorts by name within the namespace.
        ChestStorage storage = storage(1, Items.STONE, 1, 0, Items.DIRT, 1);
        assertArrayEquals(new int[]{0, 1}, head(ChestView.compute(storage, "", ChestSortMode.MOD, NAMES), 2));
    }

    @Test
    public void testSearchByNameHidesEmptyAndOtherSlots() {
        ChestStorage storage = storage(1, Items.STONE, 1, 4, Items.STONE_BRICKS, 1, 7, Items.DIRT, 1);
        assertArrayEquals(new int[]{1, 4}, ChestView.compute(storage, "Stone", ChestSortMode.NONE, NAMES));
    }

    @Test
    public void testSearchByIdAndNamespace() {
        ChestStorage storage = storage(1, Items.STONE, 1, 4, Items.DIRT, 1);
        assertArrayEquals(new int[]{1, 4}, ChestView.compute(storage, "minecraft:", ChestSortMode.NONE, stack -> "x"));
        assertArrayEquals(new int[0], ChestView.compute(storage, "nothing", ChestSortMode.NONE, NAMES));
    }

    @Test
    public void testSearchAndSortCombine() {
        ChestStorage storage = storage(1, Items.STONE_BRICKS, 3, 4, Items.STONE, 9, 7, Items.DIRT, 99);
        assertArrayEquals(new int[]{4, 1}, ChestView.compute(storage, " stone ", ChestSortMode.COUNT, NAMES));
    }

    @Test
    public void testMixedFullChest() {
        Item[] items = {Items.STONE, Items.DIRT, Items.SAND, Items.GRAVEL, Items.OAK_LOG, Items.COBBLESTONE, Items.GLASS, Items.IRON_INGOT, Items.GOLD_INGOT};
        ChestStorage storage = new ChestStorage(81, CapacityProfile.ofDepth(1000));
        for (int slot = 0; slot < 81; slot++) {
            storage.insert(slot, new ItemStack(items[slot % items.length]), slot + 1, false);
        }
        int[] byCount = ChestView.compute(storage, "", ChestSortMode.COUNT, NAMES);
        assertEquals(80, byCount[0]);
        assertEquals(0, byCount[80]);
        int[] byName = ChestView.compute(storage, "", ChestSortMode.NAME, NAMES);
        assertEquals(81, Arrays.stream(byName).distinct().count());
        assertEquals("cobblestone", NAMES.apply(storage.getSlot(byName[0]).getPrototype()));
        // Iron and gold ingots, 9 slots each.
        assertEquals(18, ChestView.compute(storage, "ingot", ChestSortMode.NONE, NAMES).length);
    }

}
