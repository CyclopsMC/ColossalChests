package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.cyclops.colossalchests2.storage.BootstrapTest;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestSearch extends BootstrapTest {

    private static final Function<ItemStack, String> NAMES = stack -> stack.is(Items.STONE_BRICKS) ? "Stone Bricks" : "Some Item";
    private static final Function<ItemStack, List<String>> TOOLTIPS = stack -> List.of(NAMES.apply(stack), "Building Blocks");
    private static final DeepSlot BRICKS = DeepSlot.of(new ItemStack(Items.STONE_BRICKS), 10);

    @Test
    public void testEmptyQueryMatchesEverything() {
        assertTrue(ChestSearch.matches(BRICKS, "", NAMES, TOOLTIPS));
        assertTrue(ChestSearch.matches(DeepSlot.EMPTY, "  ", NAMES, TOOLTIPS));
    }

    @Test
    public void testEmptySlotsMatchNoQuery() {
        assertFalse(ChestSearch.matches(DeepSlot.EMPTY, "stone", NAMES, TOOLTIPS));
    }

    @Test
    public void testNameIgnoringCaseAndSpaces() {
        assertTrue(ChestSearch.matches(BRICKS, " BRICK ", NAMES, TOOLTIPS));
        assertFalse(ChestSearch.matches(BRICKS, "dirt", NAMES, TOOLTIPS));
    }

    @Test
    public void testItemId() {
        assertTrue(ChestSearch.matches(BRICKS, "minecraft:stone_b", NAMES, TOOLTIPS));
        assertTrue(ChestSearch.matches(DeepSlot.of(new ItemStack(Items.DIRT), 1), "minecraft:", NAMES, TOOLTIPS));
    }

    @Test
    public void testLockedEmptySlotMatchesItsItem() {
        assertTrue(ChestSearch.matches(DeepSlot.of(new ItemStack(Items.STONE_BRICKS), 0, true, null), "bricks", NAMES, TOOLTIPS));
    }

    @Test
    public void testModPrefix() {
        assertTrue(ChestSearch.matches(BRICKS, "@minecraft", NAMES, TOOLTIPS));
        assertTrue(ChestSearch.matches(BRICKS, "@mine", NAMES, TOOLTIPS));
        assertFalse(ChestSearch.matches(BRICKS, "@colossalchests2", NAMES, TOOLTIPS));
    }

    @Test
    public void testTooltipPrefix() {
        assertTrue(ChestSearch.matches(BRICKS, "#building", NAMES, TOOLTIPS));
        assertFalse(ChestSearch.matches(BRICKS, "#redstone", NAMES, TOOLTIPS));
    }

    @Test
    public void testSpacesRequireAllTerms() {
        assertTrue(ChestSearch.matches(BRICKS, "stone  @minecraft", NAMES, TOOLTIPS));
        assertFalse(ChestSearch.matches(BRICKS, "stone dirt", NAMES, TOOLTIPS));
    }

    @Test
    public void testPipeAllowsAlternatives() {
        assertTrue(ChestSearch.matches(BRICKS, "dirt|bricks", NAMES, TOOLTIPS));
        assertTrue(ChestSearch.matches(BRICKS, "dirt|@minecraft stone", NAMES, TOOLTIPS));
        assertFalse(ChestSearch.matches(BRICKS, "dirt|log", NAMES, TOOLTIPS));
        assertFalse(ChestSearch.matches(BRICKS, "dirt|", NAMES, TOOLTIPS));
    }

}
