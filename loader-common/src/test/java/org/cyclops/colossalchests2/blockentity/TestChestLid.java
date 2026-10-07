package org.cyclops.colossalchests2.blockentity;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rubensworks
 */
public class TestChestLid {

    @Test
    public void testOpenAndLand() {
        ChestLid lid = new ChestLid();
        lid.setSpeed(0.25F);
        assertFalse(lid.tick());
        lid.shouldBeOpen(true);
        for (int i = 0; i < 4; i++) {
            assertFalse(lid.tick());
        }
        assertEquals(1.0F, lid.getOpenness(1), 0.0001F);
        assertEquals(0.875F, lid.getOpenness(0.5F), 0.0001F);
        lid.shouldBeOpen(false);
        for (int i = 0; i < 3; i++) {
            assertFalse("tick " + i, lid.tick());
        }
        // Lands on the fourth tick, and only reports it once.
        assertTrue(lid.tick());
        assertEquals(0.0F, lid.getOpenness(1), 0.0001F);
        assertFalse(lid.tick());
    }

    @Test
    public void testSlowerLid() {
        ChestLid lid = new ChestLid();
        lid.setSpeed(0.125F);
        lid.shouldBeOpen(true);
        int ticks = 0;
        while (lid.getOpenness(1) < 1.0F) {
            lid.tick();
            ticks++;
        }
        assertEquals(8, ticks);
    }

}
