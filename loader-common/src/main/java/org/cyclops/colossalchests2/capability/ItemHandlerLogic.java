package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;

/**
 * Item handler semantics on top of a {@link ChestStorage}, shared by the loaders that use item handlers.
 * Counts and limits above {@link Integer#MAX_VALUE} are clamped, the real values are available through
 * {@link IDeepItemStorage}.
 * @author rubensworks
 */
public class ItemHandlerLogic {

    private final ChestStorage storage;
    private final WallAccess access;

    /**
     * @param storage The storage.
     * @param access What automation may do through this handler.
     */
    public ItemHandlerLogic(ChestStorage storage, WallAccess access) {
        this.storage = storage;
        this.access = access;
    }

    public ItemHandlerLogic(ChestStorage storage) {
        this(storage, WallAccess.OPEN);
    }

    public ChestStorage getStorage() {
        return storage;
    }

    public WallAccess getAccess() {
        return access;
    }

    public int getSlots() {
        return storage.getSlotCount();
    }

    /**
     * @param slot A slot index.
     * @return A copy of the slot contents, with the count clamped to {@link Integer#MAX_VALUE}.
     */
    public ItemStack getStackInSlot(int slot) {
        ItemStack type = getExtractionType(slot);
        long available = type.isEmpty() ? 0 : storage.getAvailable(slot, type);
        return available == 0 ? ItemStack.EMPTY : type.copyWithCount(clamp(available));
    }

    /**
     * @param slot A slot index.
     * @return The type the slot is seen and extracted as: for a compressed slot the form of its family in this
     * handler's filter, else the slot's own extraction type.
     */
    public ItemStack getExtractionType(int slot) {
        DeepSlot deepSlot = storage.getSlot(slot);
        Item form = deepSlot.isEmpty() ? null : access.getExtractionForm(storage, deepSlot.getPrototype());
        return form != null ? new ItemStack(form) : storage.getExtractionType(slot);
    }

    /**
     * @param slot A slot index.
     * @param stack The stack to insert.
     * @param simulate If the storage must not change.
     * @return The remainder that could not be inserted.
     */
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!access.canInsert(storage, stack)) {
            return stack;
        }
        long inserted = storage.insertAutomated(slot, stack, stack.getCount(), simulate, access.voidFull());
        if (inserted >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(stack.getCount() - (int) inserted);
    }

    /**
     * Extract at most one stack of the slot's item type, as item handlers expect.
     * @param slot A slot index.
     * @param amount The maximum amount to extract.
     * @param simulate If the storage must not change.
     * @return The extracted stack.
     */
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack type = getExtractionType(slot);
        if (amount <= 0 || type.isEmpty() || !access.canExtract(storage, type)) {
            return ItemStack.EMPTY;
        }
        long extracted = storage.extract(slot, type, Math.min(amount, type.getMaxStackSize()), simulate);
        return extracted == 0 ? ItemStack.EMPTY : type.copyWithCount((int) extracted);
    }

    /**
     * @param slot A slot index.
     * @return The slot capacity for its current type, clamped to {@link Integer#MAX_VALUE}.
     */
    public int getSlotLimit(int slot) {
        ItemStack type = getExtractionType(slot);
        return clamp(type.isEmpty() ? storage.getCapacity(slot) : storage.getCapacity(type));
    }

    /**
     * @param slot A slot index.
     * @param stack A stack.
     * @return If the stack's type may go into the slot, ignoring how full it is.
     */
    public boolean isItemValid(int slot, ItemStack stack) {
        return access.canInsert(storage, stack) && storage.canAccept(slot, stack);
    }

    private static int clamp(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

}
