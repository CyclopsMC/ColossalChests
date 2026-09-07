package org.cyclops.colossalchests.inventory;

import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;

/**
 * An {@link InventoryColossalChest} that is identified by its instance instead of its contents.
 *
 * Fabric's {@code InventoryStorage} caches its wrappers in a map keyed on the container,
 * and compares those keys with equals.
 * {@code SimpleInventory} considers any two inventories with equally-sized and equal contents to be equal,
 * and its hash code changes on every modification,
 * so two empty chests of the same size would share a single storage wrapper,
 * and items inserted into one chest would end up in the other.
 *
 * @author rubensworks
 */
public class InventoryColossalChestFabric extends InventoryColossalChest {

    public InventoryColossalChestFabric(BlockEntityColossalChest chest, int size, int stackLimit) {
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
