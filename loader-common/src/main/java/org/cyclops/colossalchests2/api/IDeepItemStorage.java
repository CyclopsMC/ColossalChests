package org.cyclops.colossalchests2.api;

import net.minecraft.world.item.ItemStack;

/**
 * Read-only view on a deep-slot storage with true long counts.
 * Standard item handlers clamp counts to an int, this exposes the real values.
 * @author rubensworks
 */
public interface IDeepItemStorage {

    /**
     * @return The number of slots.
     */
    int getSlots();

    /**
     * @param slot A slot index.
     * @return The item type in the slot with count 1, or empty. Must not be modified.
     */
    ItemStack getType(int slot);

    /**
     * @param slot A slot index.
     * @return The amount of items in the slot.
     */
    long getCount(int slot);

    /**
     * @param slot A slot index.
     * @return The maximum amount of items the slot can hold for its current type.
     */
    long getCapacity(int slot);

    /**
     * @param slot A slot index.
     * @return If the slot is reserved for its type.
     */
    boolean isLocked(int slot);

}
