package org.cyclops.colossalchests2.storage;

/**
 * Determines how many items fit in a single slot of a {@link ChestStorage}.
 * Depth is uniform across all slots (D-2).
 * @param baseDepth Stacks per slot from the structure size.
 * @param depthUpgrades Number of Depth upgrades.
 * @param depthMultiplier Depth multiplier per Depth upgrade.
 * @param bundlingLevel Bundling upgrade level, raising the capacity of non-stackables.
 * @param maxItemsPerSlot Technical cap per slot.
 * @param acceptNonStackables If items with a max stack size of 1 are accepted at all.
 * @author rubensworks
 */
public record CapacityProfile(long baseDepth, int depthUpgrades, int depthMultiplier, int bundlingLevel,
                              long maxItemsPerSlot, boolean acceptNonStackables) {

    public static final long DEFAULT_MAX_ITEMS_PER_SLOT = Integer.MAX_VALUE;
    public static final int DEFAULT_DEPTH_MULTIPLIER = 2;

    public CapacityProfile {
        if (baseDepth < 0 || depthUpgrades < 0 || depthMultiplier < 1 || bundlingLevel < 0 || maxItemsPerSlot < 0) {
            throw new IllegalArgumentException("Invalid capacity profile");
        }
    }

    /**
     * @param baseDepth Stacks per slot.
     * @return A profile without upgrades and with default limits.
     */
    public static CapacityProfile ofDepth(long baseDepth) {
        return new CapacityProfile(baseDepth, 0, DEFAULT_DEPTH_MULTIPLIER, 0, DEFAULT_MAX_ITEMS_PER_SLOT, true);
    }

    public CapacityProfile withBaseDepth(long baseDepth) {
        return new CapacityProfile(baseDepth, depthUpgrades, depthMultiplier, bundlingLevel, maxItemsPerSlot, acceptNonStackables);
    }

    public CapacityProfile withDepthUpgrades(int depthUpgrades) {
        return new CapacityProfile(baseDepth, depthUpgrades, depthMultiplier, bundlingLevel, maxItemsPerSlot, acceptNonStackables);
    }

    public CapacityProfile withBundlingLevel(int bundlingLevel) {
        return new CapacityProfile(baseDepth, depthUpgrades, depthMultiplier, bundlingLevel, maxItemsPerSlot, acceptNonStackables);
    }

    public CapacityProfile withMaxItemsPerSlot(long maxItemsPerSlot) {
        return new CapacityProfile(baseDepth, depthUpgrades, depthMultiplier, bundlingLevel, maxItemsPerSlot, acceptNonStackables);
    }

    public CapacityProfile withAcceptNonStackables(boolean acceptNonStackables) {
        return new CapacityProfile(baseDepth, depthUpgrades, depthMultiplier, bundlingLevel, maxItemsPerSlot, acceptNonStackables);
    }

    /**
     * @return Stacks per slot, including Depth upgrades.
     */
    public long depth() {
        long depth = baseDepth;
        for (int i = 0; i < depthUpgrades; i++) {
            depth = saturatedMultiply(depth, depthMultiplier);
        }
        return depth;
    }

    /**
     * Non-stackables ignore structure size: 2^bundling * (1 + depthUpgrades).
     * @return Capacity per slot for non-stackable items.
     */
    public long nonStackableCapacity() {
        if (!acceptNonStackables) {
            return 0;
        }
        long bundled = bundlingLevel >= 62 ? Long.MAX_VALUE : 1L << bundlingLevel;
        return Math.min(saturatedMultiply(bundled, 1L + depthUpgrades), maxItemsPerSlot);
    }

    /**
     * @param maxStackSize The vanilla max stack size of an item type.
     * @return Capacity per slot for that item type.
     */
    public long capacityFor(int maxStackSize) {
        if (maxStackSize <= 1) {
            return nonStackableCapacity();
        }
        return Math.min(saturatedMultiply(depth(), maxStackSize), maxItemsPerSlot);
    }

    static long saturatedMultiply(long a, long b) {
        long high = Math.multiplyHigh(a, b);
        long low = a * b;
        if ((high == 0 && low >= 0) || (high == -1 && low < 0)) {
            return low;
        }
        return (a < 0) == (b < 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
    }

}
