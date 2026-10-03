package org.cyclops.colossalchests2.modcompat;

import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;

/**
 * Common Capabilities inventory state of a chest storage, so consumers can skip rescanning unchanged chests.
 * Only load this class when Common Capabilities is present.
 * @author rubensworks
 */
public class InventoryStateChestStorage implements IInventoryState {

    private final ChestStorage storage;

    public InventoryStateChestStorage(ChestStorage storage) {
        this.storage = storage;
    }

    @Override
    public int getState() {
        return storage.getState();
    }
}
