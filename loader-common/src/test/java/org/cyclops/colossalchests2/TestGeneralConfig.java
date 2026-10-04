package org.cyclops.colossalchests2;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestGeneralConfig {

    @Test
    public void testDepthForEverySize() {
        long[] expected = {4, 16, 64, 256, 1024, 4096, 16384, 65536, 262144};
        for (int size = GeneralConfig.MIN_SIZE; size <= GeneralConfig.HARD_MAX_SIZE; size++) {
            assertEquals("size " + size, expected[size - GeneralConfig.MIN_SIZE], GeneralConfig.getDepthForSize(size));
        }
    }

    @Test
    public void testDepthFollowsConfig() {
        int old = GeneralConfig.depthSize3;
        try {
            GeneralConfig.depthSize3 = 7;
            assertEquals(7, GeneralConfig.getDepthForSize(3));
        } finally {
            GeneralConfig.depthSize3 = old;
        }
    }

    @Test
    public void testDepthClampedToOne() {
        int old = GeneralConfig.depthSize3;
        try {
            GeneralConfig.depthSize3 = -5;
            assertEquals(1, GeneralConfig.getDepthForSize(3));
        } finally {
            GeneralConfig.depthSize3 = old;
        }
    }

    @Test
    public void testSlotsDefaults() {
        assertEquals(27, GeneralConfig.getBaseSlots());
        assertEquals(108, GeneralConfig.getMaxSlots());
    }

    @Test
    public void testSlotsClamped() {
        int oldBase = GeneralConfig.baseSlots;
        int oldMax = GeneralConfig.maxSlots;
        try {
            GeneralConfig.maxSlots = 500;
            GeneralConfig.baseSlots = 0;
            assertEquals(GeneralConfig.HARD_MAX_SLOTS, GeneralConfig.getMaxSlots());
            assertEquals(1, GeneralConfig.getBaseSlots());
            GeneralConfig.maxSlots = 9;
            GeneralConfig.baseSlots = 27;
            assertEquals(9, GeneralConfig.getBaseSlots());
        } finally {
            GeneralConfig.baseSlots = oldBase;
            GeneralConfig.maxSlots = oldMax;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDepthTooSmall() {
        GeneralConfig.getDepthForSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDepthTooLarge() {
        GeneralConfig.getDepthForSize(11);
    }

}
