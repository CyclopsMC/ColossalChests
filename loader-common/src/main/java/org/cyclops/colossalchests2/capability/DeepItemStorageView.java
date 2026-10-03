package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;

/**
 * {@link IDeepItemStorage} backed by a {@link ChestStorage}.
 * @author rubensworks
 */
public class DeepItemStorageView implements IDeepItemStorage {

    private final ChestStorage storage;

    public DeepItemStorageView(ChestStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getSlots() {
        return storage.getSlotCount();
    }

    @Override
    public ItemStack getType(int slot) {
        return storage.getSlot(slot).getPrototype();
    }

    @Override
    public long getCount(int slot) {
        return storage.getSlot(slot).getCount();
    }

    @Override
    public long getCapacity(int slot) {
        return storage.getCapacity(slot);
    }

    @Override
    public boolean isLocked(int slot) {
        return storage.getSlot(slot).isLocked();
    }

}
