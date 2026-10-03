package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable slot of a {@link ChestStorage}: one item type with a long count.
 * A slot with a prototype and a zero count only exists while it is locked.
 * @author rubensworks
 */
public final class DeepSlot {

    public static final DeepSlot EMPTY = new DeepSlot(ItemStack.EMPTY, 0, false, null);

    private final ItemStack prototype;
    private final long count;
    private final boolean locked;
    @Nullable
    private final Item compressionForm;

    private DeepSlot(ItemStack prototype, long count, boolean locked, @Nullable Item compressionForm) {
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
     * @param compressionForm The item of the compression form to extract in, or null (or air) for the default.
     * @return The slot, {@link #EMPTY} if nothing is stored and nothing is reserved.
     */
    public static DeepSlot of(ItemStack prototype, long count, boolean locked, @Nullable Item compressionForm) {
        if (count < 0) {
            throw new IllegalArgumentException("Negative count: " + count);
        }
        if (prototype.isEmpty() || (count == 0 && !locked)) {
            return EMPTY;
        }
        return new DeepSlot(prototype.copyWithCount(1), count, locked, compressionForm == Items.AIR ? null : compressionForm);
    }

    public static DeepSlot of(ItemStack prototype, long count) {
        return of(prototype, count, false, null);
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

    /**
     * The form, as an item, that the player picked to extract this slot's contents in.
     * Stored as an item rather than a position in the compression family, so it survives datapack changes to
     * the family. If the item is no longer a form of the slot's family, the largest form should be used.
     * @return The picked form, or empty for the default (the largest form).
     */
    public Optional<Item> getCompressionForm() {
        return Optional.ofNullable(compressionForm);
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

    public DeepSlot withCompressionForm(@Nullable Item compressionForm) {
        return of(prototype, count, locked, compressionForm);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeepSlot that
                && this.count == that.count
                && this.locked == that.locked
                && Objects.equals(this.compressionForm, that.compressionForm)
                && ItemStack.isSameItemSameComponents(this.prototype, that.prototype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemStack.hashItemAndComponents(prototype), count, locked, compressionForm);
    }

    @Override
    public String toString() {
        return "DeepSlot{" + (isEmpty() ? "empty" : prototype.getItem() + " x" + count)
                + (locked ? ", locked" : "") + (compressionForm != null ? ", form=" + compressionForm : "") + "}";
    }
}
