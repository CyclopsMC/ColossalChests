package org.cyclops.colossalchests2.storage;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestCapacityProfile {

    @Test
    public void testDepthWithoutUpgrades() {
        assertEquals(16, CapacityProfile.ofDepth(16).depth());
    }

    @Test
    public void testDepthWithUpgrades() {
        assertEquals(128, CapacityProfile.ofDepth(16).withDepthUpgrades(3).depth());
    }

    @Test
    public void testDepthSaturates() {
        assertEquals(Long.MAX_VALUE, CapacityProfile.ofDepth(Long.MAX_VALUE / 2).withDepthUpgrades(4).depth());
    }

    @Test
    public void testCapacityStackable() {
        CapacityProfile profile = CapacityProfile.ofDepth(16);
        assertEquals(1024, profile.capacityFor(64));
        assertEquals(256, profile.capacityFor(16));
    }

    @Test
    public void testCapacityClampedToMaxItemsPerSlot() {
        CapacityProfile profile = CapacityProfile.ofDepth(262144).withDepthUpgrades(7);
        assertEquals(Integer.MAX_VALUE, profile.capacityFor(64));
        assertEquals(100, profile.withMaxItemsPerSlot(100).capacityFor(64));
    }

    @Test
    public void testCapacityNonStackable() {
        CapacityProfile profile = CapacityProfile.ofDepth(262144);
        assertEquals(1, profile.capacityFor(1));
        assertEquals(2, profile.withDepthUpgrades(1).capacityFor(1));
        assertEquals(16, profile.withBundlingLevel(4).capacityFor(1));
        assertEquals(112, profile.withBundlingLevel(4).withDepthUpgrades(6).capacityFor(1));
        assertEquals(0, profile.withAcceptNonStackables(false).capacityFor(1));
        assertEquals(10, profile.withBundlingLevel(4).withMaxItemsPerSlot(10).capacityFor(1));
    }

    @Test
    public void testNonStackableCapacityIgnoresBaseDepth() {
        assertEquals(CapacityProfile.ofDepth(4).nonStackableCapacity(), CapacityProfile.ofDepth(65536).nonStackableCapacity());
    }

    @Test
    public void testWithers() {
        CapacityProfile profile = CapacityProfile.ofDepth(4)
                .withBaseDepth(8).withDepthUpgrades(1).withBundlingLevel(2).withMaxItemsPerSlot(99).withAcceptNonStackables(false);
        assertEquals(new CapacityProfile(8, 1, 2, 2, 99, false), profile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid() {
        new CapacityProfile(-1, 0, 2, 0, 1, true);
    }

    @Test
    public void testSaturatedMultiply() {
        assertEquals(6, CapacityProfile.saturatedMultiply(2, 3));
        assertEquals(Long.MAX_VALUE, CapacityProfile.saturatedMultiply(Long.MAX_VALUE, 2));
        assertEquals(Long.MIN_VALUE, CapacityProfile.saturatedMultiply(Long.MAX_VALUE, -2));
    }

}
