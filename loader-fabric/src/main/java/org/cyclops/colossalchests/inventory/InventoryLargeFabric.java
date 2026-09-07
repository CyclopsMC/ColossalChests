package org.cyclops.colossalchests.inventory;

import org.cyclops.cyclopscore.inventory.LargeInventoryCommon;

/**
 * A {@link LargeInventoryCommon} that is identified by its instance instead of its contents.
 *
 * See {@link InventoryColossalChestFabric} for why this is needed.
 *
 * @author rubensworks
 */
public class InventoryLargeFabric extends LargeInventoryCommon {

    public InventoryLargeFabric(int size, int stackLimit) {
        super(size, stackLimit);
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
