package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * NeoForge item handler on a chest storage.
 * @author rubensworks
 */
public class ItemHandlerChestStorage implements IItemHandler {

    private final ItemHandlerLogic logic;

    public ItemHandlerChestStorage(ItemHandlerLogic logic) {
        this.logic = logic;
    }

    @Override
    public int getSlots() {
        return logic.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return logic.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return logic.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return logic.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return logic.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return logic.isItemValid(slot, stack);
    }
}
