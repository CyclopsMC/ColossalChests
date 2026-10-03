package org.cyclops.colossalchests2.storage;

/**
 * Determines how many items fit in a single slot of a {@link ChestStorage}.
 * Depth is uniform across all slots.
 * Profiles are built with a {@link Builder}: the structure sets the base depth,
 * and each installed upgrade applies its own modifiers, so the profile knows nothing about upgrade types.
 * @param depth Stacks per slot for stackable items.
 * @param maxItemsPerSlot Technical cap per slot.
 * @param acceptNonStackables If items with a max stack size of 1 are accepted at all.
 * @param nonStackableCapacity Items per slot for non-stackable items, independent of depth.
 * @author rubensworks
 */
public record CapacityProfile(long depth, long maxItemsPerSlot, boolean acceptNonStackables, long nonStackableCapacity) {

    public static final long DEFAULT_MAX_ITEMS_PER_SLOT = Integer.MAX_VALUE;

    public CapacityProfile {
        if (depth < 0 || maxItemsPerSlot < 0 || nonStackableCapacity < 0) {
            throw new IllegalArgumentException("Invalid capacity profile");
        }
    }

    /**
     * @param depth Stacks per slot.
     * @return A profile without modifiers and with default limits.
     */
    public static CapacityProfile ofDepth(long depth) {
        return builder(depth).build();
    }

    /**
     * @param baseDepth Stacks per slot from the structure size.
     * @return A builder to apply modifiers on.
     */
    public static Builder builder(long baseDepth) {
        return new Builder(baseDepth);
    }

    public CapacityProfile withDepth(long depth) {
        return new CapacityProfile(depth, maxItemsPerSlot, acceptNonStackables, nonStackableCapacity);
    }

    public CapacityProfile withMaxItemsPerSlot(long maxItemsPerSlot) {
        return new CapacityProfile(depth, maxItemsPerSlot, acceptNonStackables, nonStackableCapacity);
    }

    public CapacityProfile withAcceptNonStackables(boolean acceptNonStackables) {
        return new CapacityProfile(depth, maxItemsPerSlot, acceptNonStackables, nonStackableCapacity);
    }

    public CapacityProfile withNonStackableCapacity(long nonStackableCapacity) {
        return new CapacityProfile(depth, maxItemsPerSlot, acceptNonStackables, nonStackableCapacity);
    }

    /**
     * @param maxStackSize The vanilla max stack size of an item type.
     * @return Capacity per slot for that item type.
     */
    public long capacityFor(int maxStackSize) {
        if (maxStackSize <= 1) {
            return acceptNonStackables ? Math.min(nonStackableCapacity, maxItemsPerSlot) : 0;
        }
        return Math.min(saturatedMultiply(depth, maxStackSize), maxItemsPerSlot);
    }

    static long saturatedMultiply(long a, long b) {
        long high = Math.multiplyHigh(a, b);
        long low = a * b;
        if ((high == 0 && low >= 0) || (high == -1 && low < 0)) {
            return low;
        }
        return (a < 0) == (b < 0) ? Long.MAX_VALUE : Long.MIN_VALUE;
    }

    static long saturatedAdd(long a, long b) {
        long sum = a + b;
        if (((a ^ sum) & (b ^ sum)) < 0) {
            return a < 0 ? Long.MIN_VALUE : Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * Collects modifiers from the structure and installed upgrades.
     * Non-stackable capacity is (product of multipliers) * (1 + sum of factors), so for example
     * Bundling multiplies by 2 per level and Depth adds 1 per upgrade.
     * All math saturates instead of overflowing.
     */
    public static final class Builder {

        private long depth;
        private long nonStackableMultiplier = 1;
        private long nonStackableFactor = 1;
        private long maxItemsPerSlot = DEFAULT_MAX_ITEMS_PER_SLOT;
        private boolean acceptNonStackables = true;

        private Builder(long baseDepth) {
            if (baseDepth < 0) {
                throw new IllegalArgumentException("Negative depth: " + baseDepth);
            }
            this.depth = baseDepth;
        }

        public Builder multiplyDepth(long factor) {
            this.depth = saturatedMultiply(depth, requirePositive(factor));
            return this;
        }

        public Builder multiplyNonStackable(long factor) {
            this.nonStackableMultiplier = saturatedMultiply(nonStackableMultiplier, requirePositive(factor));
            return this;
        }

        public Builder addNonStackableFactor(long amount) {
            this.nonStackableFactor = saturatedAdd(nonStackableFactor, requirePositive(amount));
            return this;
        }

        public Builder maxItemsPerSlot(long maxItemsPerSlot) {
            this.maxItemsPerSlot = maxItemsPerSlot;
            return this;
        }

        public Builder acceptNonStackables(boolean acceptNonStackables) {
            this.acceptNonStackables = acceptNonStackables;
            return this;
        }

        public CapacityProfile build() {
            return new CapacityProfile(depth, maxItemsPerSlot, acceptNonStackables,
                    saturatedMultiply(nonStackableMultiplier, nonStackableFactor));
        }

        private static long requirePositive(long value) {
            if (value < 1) {
                throw new IllegalArgumentException("Modifiers must be positive: " + value);
            }
            return value;
        }
    }

}
