package org.cyclops.colossalchests2.inventory;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestLayout {

    @Test
    public void testSizes() {
        assertEquals(new ChestLayout(27, 9, 3), ChestLayout.of(27));
        assertEquals(new ChestLayout(54, 9, 6), ChestLayout.of(54));
        assertEquals(new ChestLayout(81, 18, 5), ChestLayout.of(81));
        assertEquals(new ChestLayout(28, 9, 4), ChestLayout.of(28));
    }

    @Test
    public void testFitsSmallestGuiScale() {
        // The scaled GUI is at least 240 pixels high.
        for (int slots : new int[]{27, 54, 81}) {
            assertTrue("height for " + slots, ChestLayout.of(slots).getHeight() <= 240);
        }
        assertEquals(176, ChestLayout.of(27).getWidth());
        assertEquals(338, ChestLayout.of(81).getWidth());
    }

    @Test
    public void testPlayerInventoryCentered() {
        assertEquals(8, ChestLayout.of(27).getPlayerInventoryX());
        assertEquals(89, ChestLayout.of(81).getPlayerInventoryX());
    }

    @Test
    public void testPositionAt() {
        ChestLayout layout = ChestLayout.of(27);
        assertEquals(0, layout.getPositionAt(layout.getSlotX(0), layout.getSlotY(0)));
        assertEquals(10, layout.getPositionAt(layout.getSlotX(10) + 15, layout.getSlotY(10) + 15));
        assertEquals(-1, layout.getPositionAt(layout.getSlotX(0) - 3, layout.getSlotY(0)));
        assertEquals(-1, layout.getPositionAt(layout.getSlotX(26), layout.getSlotY(26) + 18));
        // The last row of an 81-slot grid is half full.
        ChestLayout large = ChestLayout.of(81);
        assertEquals(80, large.getPositionAt(large.getSlotX(80), large.getSlotY(80)));
        assertEquals(-1, large.getPositionAt(large.getSlotX(80) + 18, large.getSlotY(80)));
    }

}
