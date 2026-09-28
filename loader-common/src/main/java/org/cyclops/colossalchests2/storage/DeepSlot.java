package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * An immutable slot of a {@link ChestStorage}: one item type with a long count.
 * A slot with a prototype and a zero count only exists while it is locked.
 * @author rubensworks
 */
public final class DeepSlot {

    public static final int FORM_DEFAULT = -1;
    public static final DeepSlot EMPTY = new DeepSlot(ItemStack.EMPTY, 0, false, FORM_DEFAULT);

    private final ItemStack prototype;
    private final long count;
    private final boolean locked;
    private final int compressionForm;

    private DeepSlot(ItemStack prototype, long count, boolean locked, int compressionForm) {
        this.prototype = prototype;
        this.count = count;
        this.locked = locked;
        this.compressionForm = compressionForm;
    }

    /**
     * Create a normalized slot.
     * @param prototype The item type, its count is ignored.
     * @param count The amount of items.
     * @param locked If the slot is locked to its type.
     * @param compressionForm The selected compression form, or {@link #FORM_DEFAULT}.
     * @return The slot, {@link #EMPTY} if nothing is stored and nothing is reserved.
     */
    public static DeepSlot of(ItemStack prototype, long count, boolean locked, int compressionForm) {
        if (count < 0) {
            throw new IllegalArgumentException("Negative count: " + count);
        }
        if (prototype.isEmpty() || (count == 0 && !locked)) {
            return EMPTY;
        }
        return new DeepSlot(prototype.copyWithCount(1), count, locked, compressionForm);
    }

    public static DeepSlot of(ItemStack prototype, long count) {
        return of(prototype, count, false, FORM_DEFAULT);
    }

    /**
     * @return The item type with count 1. Do not modify.
     */
    public ItemStack getPrototype() {
        return prototype;
    }

    public long getCount() {
        return count;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getCompressionForm() {
        return compressionForm;
    }

    /**
     * @return If there is no item type in this slot.
     */
    public boolean isEmpty() {
        return prototype.isEmpty();
    }

    /**
     * @param type An item type.
     * @return If the given type is the type of this slot.
     */
    public boolean matches(ItemStack type) {
        return !isEmpty() && ItemStack.isSameItemSameComponents(prototype, type);
    }

    public DeepSlot withCount(long count) {
        return of(prototype, count, locked, compressionForm);
    }

    public DeepSlot withLocked(boolean locked) {
        return of(prototype, count, locked, compressionForm);
    }

    public DeepSlot withCompressionForm(int compressionForm) {
        return of(prototype, count, locked, compressionForm);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeepSlot that
                && this.count == that.count
                && this.locked == that.locked
                && this.compressionForm == that.compressionForm
                && ItemStack.isSameItemSameComponents(this.prototype, that.prototype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemStack.hashItemAndComponents(prototype), count, locked, compressionForm);
    }

    @Override
    public String toString() {
        return "DeepSlot{" + (isEmpty() ? "empty" : prototype.getItem() + " x" + count)
                + (locked ? ", locked" : "") + (compressionForm != FORM_DEFAULT ? ", form=" + compressionForm : "") + "}";
    }
}
