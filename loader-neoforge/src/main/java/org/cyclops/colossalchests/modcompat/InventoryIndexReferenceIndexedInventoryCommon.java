package org.cyclops.colossalchests.modcompat;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.cyclopscore.inventory.IInventoryIndexReference;
import org.cyclops.cyclopscore.inventory.IndexedInventory;

import java.util.Map;
import java.util.PrimitiveIterator;

/**
 * @author rubensworks
 */
public class InventoryIndexReferenceIndexedInventoryCommon implements IInventoryIndexReference {

    private final IndexedInventory inventory;

    public InventoryIndexReferenceIndexedInventoryCommon(IndexedInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public int getInventoryReferenceStackLimit() {
        return this.inventory.getInventoryReferenceStackLimit();
    }

    @Override
    public Map<Item, Int2ObjectMap<ItemStack>> getIndex() {
        return this.inventory.getIndex();
    }

    @Override
    public PrimitiveIterator.OfInt getEmptySlots() {
        return this.inventory.getEmptySlots();
    }

    @Override
    public PrimitiveIterator.OfInt getNonEmptySlots() {
        return this.inventory.getNonEmptySlots();
    }
}
