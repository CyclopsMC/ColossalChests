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
        ItemStack type = getExtractionType(slot);
        long available = type.isEmpty() ? 0 : storage.getAvailable(slot, type);
        return available == 0 ? ItemStack.EMPTY : type.copyWithCount(clamp(available));
    }

    /**
     * @param slot A slot index.
     * @return The type the slot is seen and extracted as: this handler's extraction form for a compressed slot of its
     * family, else the slot's own extraction type.
     */
    public ItemStack getExtractionType(int slot) {
        if (extractionForm != null) {
            DeepSlot deepSlot = storage.getSlot(slot);
            boolean ofFamily = storage.getFamily(deepSlot.getPrototype()).map(family -> family.indexOf(extractionForm) >= 0).orElse(false);
            if (ofFamily) {
                return new ItemStack(extractionForm);
            }
        }
        return storage.getExtractionType(slot);
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
        ItemStack type = getExtractionType(slot);
        if (amount <= 0 || type.isEmpty()) {
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
        return storage.canAccept(slot, stack);
    }

    private static int clamp(long value) {
        return (int) Math.min(value, Integer.MAX_VALUE);
    }

}
