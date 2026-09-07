package org.cyclops.colossalchests.inventory;

import org.cyclops.cyclopscore.inventory.IndexedInventoryCommon;

/**
 * An {@link IndexedInventoryCommon} that is identified by its instance instead of its contents.
 *
 * See {@link InventoryIdentitySimple} for why this is needed.
 *
 * @author rubensworks
 */
public class InventoryIdentityIndexed extends IndexedInventoryCommon {

    public InventoryIdentityIndexed(int size, int stackLimit) {
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
