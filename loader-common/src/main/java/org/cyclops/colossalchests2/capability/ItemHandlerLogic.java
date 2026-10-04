package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.jetbrains.annotations.Nullable;

/**
 * Item handler semantics on top of a {@link ChestStorage}, shared by the loaders that use item handlers.
 * Counts and limits above {@link Integer#MAX_VALUE} are clamped, the real values are available through
 * {@link IDeepItemStorage}.
 * @author rubensworks
 */
public class ItemHandlerLogic {

    private final ChestStorage storage;
    @Nullable
    private final Item extractionForm;

    /**
     * @param storage The storage.
     * @param extractionForm The compression form to extract in, or null for the largest form.
     *                       Only has an effect on compressed slots.
     */
    public ItemHandlerLogic(ChestStorage storage, @Nullable Item extractionForm) {
        this.storage = storage;
        this.extractionForm = extractionForm;
    }

    public ItemHandlerLogic(ChestStorage storage) {
        this(storage, null);
    }

    public ChestStorage getStorage() {
        return storage;
    }

    @Nullable
    public Item getExtractionForm() {
        return extractionForm;
    }

    public int getSlots() {
        return storage.getSlotCount();
    }

    /**
     * @param slot A slot index.
     * @return A copy of the slot contents, with the count clamped to {@link Integer#MAX_VALUE}.
     */
    public ItemStack getStackInSlot(int slot) {
        DeepSlot deepSlot = storage.getSlot(slot);
        if (deepSlot.getCount() == 0) {
            return ItemStack.EMPTY;
        }
        return deepSlot.getPrototype().copyWithCount(clamp(deepSlot.getCount()));
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
        long inserted = storage.insertAutomated(slot, stack, stack.getCount(), simulate);
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
        DeepSlot deepSlot = storage.getSlot(slot);
        if (amount <= 0 || deepSlot.getCount() == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack prototype = deepSlot.getPrototype();
        long extracted = storage.extract(slot, Math.min(amount, prototype.getMaxStackSize()), simulate);
        return extracted == 0 ? ItemStack.EMPTY : prototype.copyWithCount((int) extracted);
    }

    /**
     * @param slot A slot index.
     * @return The slot capacity for its current type, clamped to {@link Integer#MAX_VALUE}.
     */
    public int getSlotLimit(int slot) {
        return clamp(storage.getCapacity(slot));
    }

    /**
     * @param slot A slot index.
     * @param stack A stack.
     * @return If the stack's type may go into the slot, ignoring how full it is.
     */
    public boolean isItemValid(int slot, ItemStack stack) {
        return storage.canAccept(slot, stack);
    }

    private static int clamp(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

}
