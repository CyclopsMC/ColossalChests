package org.cyclops.colossalchests.inventory;

import org.cyclops.cyclopscore.inventory.SimpleInventoryCommon;

/**
 * A {@link SimpleInventoryCommon} that is identified by its instance instead of its contents.
 *
 * {@link SimpleInventoryCommon} considers any two inventories with equally-sized and equal contents
 * to be equal, and its hash code changes on every modification.
 * That makes it unusable as a key in the (equality-based) container-to-storage caches
 * that item transfer APIs such as Fabric's {@code InventoryStorage} maintain:
 * two empty chests of the same size would share a single storage wrapper,
 * so items inserted into one chest would end up in the other.
 *
 * @author rubensworks
 */
public class InventoryIdentitySimple extends SimpleInventoryCommon {

    public InventoryIdentitySimple(int size, int stackLimit) {
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
