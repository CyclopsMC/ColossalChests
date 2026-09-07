package org.cyclops.colossalchests.inventory;

import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;

/**
 * An {@link InventoryUncolossalChest} that is identified by its instance instead of its contents.
 *
 * See {@link InventoryColossalChestFabric} for why this is needed.
 *
 * @author rubensworks
 */
public class InventoryUncolossalChestFabric extends InventoryUncolossalChest {

    public InventoryUncolossalChestFabric(BlockEntityUncolossalChest chest, int size, int stackLimit) {
        super(chest, size, stackLimit);
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
