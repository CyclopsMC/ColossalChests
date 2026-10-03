package org.cyclops.colossalchests2.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestCapacityProfile {

    /**
     * The D-2 rules, expressed as the modifiers the Depth and Bundling upgrades will apply.
     */
    static CapacityProfile.Builder upgraded(long baseDepth, int depthUpgrades, int bundlingLevel) {
        CapacityProfile.Builder builder = CapacityProfile.builder(baseDepth);
        for (int i = 0; i < depthUpgrades; i++) {
            builder.multiplyDepth(2).addNonStackableFactor(1);
        }
        for (int i = 0; i < bundlingLevel; i++) {
            builder.multiplyNonStackable(2);
        }
        return builder;
    }

    @Test
    public void testOfDepth() {
        CapacityProfile profile = CapacityProfile.ofDepth(16);
        assertEquals(new CapacityProfile(16, 1, CapacityProfile.DEFAULT_MAX_ITEMS_PER_SLOT, true), profile);
    }

    @Test
    public void testDepthModifiers() {
        assertEquals(128, upgraded(16, 3, 0).build().depth());
    }

    @Test
    public void testDepthSaturates() {
        assertEquals(Long.MAX_VALUE, upgraded(Long.MAX_VALUE / 2, 4, 0).build().depth());
    }

    @Test
    public void testCapacityStackable() {
        CapacityProfile profile = CapacityProfile.ofDepth(16);
        assertEquals(1024, profile.capacityFor(64));
        assertEquals(256, profile.capacityFor(16));
    }

    @Test
    public void testCapacityClampedToMaxItemsPerSlot() {
        assertEquals(Integer.MAX_VALUE, upgraded(262144, 7, 0).build().capacityFor(64));
        assertEquals(100, upgraded(262144, 7, 0).maxItemsPerSlot(100).build().capacityFor(64));
    }

    @Test
    public void testCapacityNonStackable() {
        assertEquals(1, upgraded(262144, 0, 0).build().capacityFor(1));
        assertEquals(2, upgraded(262144, 1, 0).build().capacityFor(1));
        assertEquals(16, upgraded(262144, 0, 4).build().capacityFor(1));
        assertEquals(112, upgraded(262144, 6, 4).build().capacityFor(1));
        assertEquals(0, upgraded(262144, 6, 4).acceptNonStackables(false).build().capacityFor(1));
        assertEquals(10, upgraded(262144, 0, 4).maxItemsPerSlot(10).build().capacityFor(1));
    }

    @Test
    public void testNonStackableCapacityIgnoresBaseDepth() {
        assertEquals(upgraded(4, 2, 1).build().nonStackableCapacity(), upgraded(65536, 2, 1).build().nonStackableCapacity());
    }

    @Test
    public void testNonStackableSaturates() {
        CapacityProfile.Builder builder = CapacityProfile.builder(1).addNonStackableFactor(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, builder.multiplyNonStackable(4).build().nonStackableCapacity());
    }

    @Test
    public void testWithers() {
        CapacityProfile profile = CapacityProfile.ofDepth(4)
                .withDepth(8).withNonStackableCapacity(3).withMaxItemsPerSlot(99).withAcceptNonStackables(false);
        assertEquals(new CapacityProfile(8, 3, 99, false), profile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid() {
        new CapacityProfile(-1, 1, 1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidBaseDepth() {
        CapacityProfile.builder(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidModifier() {
        CapacityProfile.builder(1).multiplyDepth(0);
    }

    @Test
    public void testSaturatedMath() {
        assertEquals(6, CapacityProfile.saturatedMultiply(2, 3));
        assertEquals(Long.MAX_VALUE, CapacityProfile.saturatedMultiply(Long.MAX_VALUE, 2));
        assertEquals(Long.MIN_VALUE, CapacityProfile.saturatedMultiply(Long.MAX_VALUE, -2));
        assertEquals(5, CapacityProfile.saturatedAdd(2, 3));
        assertEquals(Long.MAX_VALUE, CapacityProfile.saturatedAdd(Long.MAX_VALUE, 1));
        assertEquals(Long.MIN_VALUE, CapacityProfile.saturatedAdd(Long.MIN_VALUE, -1));
    }

}
