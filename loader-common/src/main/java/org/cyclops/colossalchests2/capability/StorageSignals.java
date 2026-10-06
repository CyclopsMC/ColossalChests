package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.colossalchests2.storage.DisplayStats;

/**
 * Redstone signals derived from a {@link ChestStorage}.
 * @author rubensworks
 */
public class StorageSignals {

    /**
     * Comparator signal following the vanilla container formula, using the deep slot capacities:
     * 0 when empty, otherwise 1 to 15 by the average fill ratio of all slots.
     * Vanilla and loader helpers divide by the item's max stack size, which overshoots for deep slots.
     * @param storage A storage.
     * @return A signal from 0 to 15.
     */
    public static int getComparatorSignal(ChestStorage storage) {
        int slots = storage.getSlotCount();
        if (slots == 0) {
            return 0;
        }
        double fill = 0;
        boolean any = false;
        for (int slot = 0; slot < slots; slot++) {
            DeepSlot deepSlot = storage.getSlot(slot);
            if (deepSlot.getCount() > 0) {
                any = true;
                long capacity = storage.getCapacity(slot);
                fill += capacity <= 0 ? 1 : Math.min(1, (double) deepSlot.getCount() / capacity);
            }
        }
        if (!any) {
            return 0;
        }
        return (int) Math.floor(fill / slots * 14.0) + 1;
    }

    /**
     * Comparator signal for one item type: 0 when the chest holds none, otherwise 1 to 15 by how full the slots
     * holding it are.
     * @param storage A storage.
     * @param type An item type.
     * @return A signal from 0 to 15.
     */
    public static int getComparatorSignal(ChestStorage storage, ItemStack type) {
        DisplayStats stats = DisplayStats.of(storage, type);
        return stats.count() <= 0 ? 0 : (int) Math.floor(stats.getFillLevel() * 14.0) + 1;
    }

}
