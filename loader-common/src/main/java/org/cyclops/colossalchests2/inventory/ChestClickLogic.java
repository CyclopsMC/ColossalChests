package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;

/**
 * The click rules of chest slots in the GUI. Stacks handed to the cursor or player never exceed the
 * vanilla max stack size.
 * @author rubensworks
 */
public final class ChestClickLogic {

    private ChestClickLogic() {
    }

    /**
     * Apply a click on a chest slot.
     * @param storage The storage.
     * @param slot The clicked slot.
     * @param action The click.
     * @param cursor The stack on the cursor, not modified.
     * @param inventory The player inventory, for moving clicks.
     * @return The new cursor stack.
     */
    public static ItemStack click(ChestStorage storage, int slot, ChestClickAction action, ItemStack cursor, IPlayerInventory inventory) {
        return switch (action) {
            case TAKE_STACK -> cursor.isEmpty() ? take(storage, slot, false) : insert(storage, slot, cursor, cursor.getCount());
            case TAKE_HALF -> cursor.isEmpty() ? take(storage, slot, true) : insert(storage, slot, cursor, 1);
            case MOVE_STACK -> {
                move(storage, slot, inventory);
                yield cursor;
            }
            case MOVE_ALL -> {
                while (move(storage, slot, inventory)) {
                    // Keep moving stacks until the slot is empty or the inventory is full.
                }
                yield cursor;
            }
        };
    }

    /**
     * Spread the cursor over slots, like dragging a stack over vanilla slots: evenly with the left button,
     * one item per slot with the right button. Slots that cannot take the item are skipped.
     * @param storage The storage.
     * @param slots The dragged slots, in drag order.
     * @param oneEach If each slot gets one item, otherwise the cursor is split evenly.
     * @param cursor The stack on the cursor, not modified.
     * @return The new cursor stack.
     */
    public static ItemStack drag(ChestStorage storage, int[] slots, boolean oneEach, ItemStack cursor) {
        return drag(slots, oneEach, cursor, (slot, amount) -> storage.insert(slot, cursor, amount, false));
    }

    /**
     * {@link #drag(ChestStorage, int[], boolean, ItemStack)} through an inserter, so the GUI can preview a drag.
     * @param slots The dragged slots, in drag order.
     * @param oneEach If each slot gets one item, otherwise the cursor is split evenly.
     * @param cursor The stack on the cursor, not modified.
     * @param inserter Inserts into a slot and returns how much went in.
     * @return The new cursor stack.
     */
    public static ItemStack drag(int[] slots, boolean oneEach, ItemStack cursor, Inserter inserter) {
        if (cursor.isEmpty() || slots.length == 0) {
            return cursor;
        }
        int remaining = cursor.getCount();
        int perSlot = oneEach ? 1 : Math.max(1, remaining / slots.length);
        for (int slot : slots) {
            if (remaining <= 0) {
                break;
            }
            remaining -= (int) inserter.insert(slot, Math.min(perSlot, remaining));
        }
        return cursor.copyWithCount(remaining);
    }

    @FunctionalInterface
    public interface Inserter {
        long insert(int slot, int amount);
    }

    private static int maxStack(DeepSlot deepSlot) {
        return (int) Math.min(deepSlot.getPrototype().getMaxStackSize(), deepSlot.getCount());
    }

    private static ItemStack take(ChestStorage storage, int slot, boolean half) {
        DeepSlot deepSlot = storage.getSlot(slot);
        if (deepSlot.getCount() <= 0) {
            return ItemStack.EMPTY;
        }
        int amount = maxStack(deepSlot);
        if (half) {
            amount = (amount + 1) / 2;
        }
        ItemStack prototype = deepSlot.getPrototype().copy();
        long extracted = storage.extract(slot, amount, false);
        return prototype.copyWithCount((int) extracted);
    }

    private static ItemStack insert(ChestStorage storage, int slot, ItemStack cursor, int amount) {
        // The clicked slot first, then wherever the storage would put it.
        long inserted = storage.insert(slot, cursor, amount, false);
        if (inserted < amount) {
            inserted += storage.insert(cursor, amount - inserted, false);
        }
        return cursor.copyWithCount(cursor.getCount() - (int) inserted);
    }

    /**
     * @return If anything moved.
     */
    private static boolean move(ChestStorage storage, int slot, IPlayerInventory inventory) {
        DeepSlot deepSlot = storage.getSlot(slot);
        if (deepSlot.getCount() <= 0) {
            return false;
        }
        int amount = maxStack(deepSlot);
        ItemStack remainder = inventory.add(deepSlot.getPrototype().copyWithCount(amount));
        int moved = amount - remainder.getCount();
        if (moved <= 0) {
            return false;
        }
        storage.extract(slot, moved, false);
        return true;
    }

    /**
     * Where moving clicks put items.
     */
    @FunctionalInterface
    public interface IPlayerInventory {
        /**
         * @param stack A stack, which may be modified.
         * @return What did not fit.
         */
        ItemStack add(ItemStack stack);
    }

}
