package org.cyclops.colossalchests2.api.upgrade;

/**
 * Changes to a chest's capacity per slot, from {@link ChestUpgrade#applyProfile}.
 * @author rubensworks
 */
public interface ICapacityModifiers {

    /**
     * Multiply the stacks per slot.
     */
    ICapacityModifiers multiplyDepth(long factor);

    /**
     * Multiply how many unstackable items fit in a slot.
     */
    ICapacityModifiers multiplyNonStackable(long factor);

    /**
     * Add to the factor of unstackable items per slot, which starts at 1.
     */
    ICapacityModifiers addNonStackableFactor(long amount);

}
