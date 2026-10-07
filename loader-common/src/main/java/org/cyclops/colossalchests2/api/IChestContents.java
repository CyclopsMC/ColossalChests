package org.cyclops.colossalchests2.api;

import net.minecraft.world.item.ItemStack;

/**
 * The slots of a chest. Slots hold one item type each, in amounts that can exceed a stack.
 * @author rubensworks
 */
public interface IChestContents {

    int getSlotCount();

    /**
     * @return The item type in a slot, empty for an empty slot. Its count is meaningless. Do not modify.
     */
    ItemStack getSlotType(int slot);

    /**
     * @return The amount of items in a slot.
     */
    long getSlotAmount(int slot);

}
