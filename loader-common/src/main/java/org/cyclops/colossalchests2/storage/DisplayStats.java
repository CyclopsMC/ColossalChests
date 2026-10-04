package org.cyclops.colossalchests2.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * What a Display wall shows about one item type in a chest.
 * @param count How many of the type the chest holds, across all slots and compression forms.
 * @param capacity How many of the type the slots holding it can hold, 0 if none hold it.
 * @param locked If a slot holding the type is locked.
 * @param voided If a slot holding the type is voiding.
 * @param compressed If the type is stored compressed.
 * @author rubensworks
 */
public record DisplayStats(long count, long capacity, boolean locked, boolean voided, boolean compressed) {

    public static final DisplayStats EMPTY = new DisplayStats(0, 0, false, false, false);

    /**
     * @param storage A storage.
     * @param type An item type, empty for none.
     * @return The stats of the type in the storage.
     */
    public static DisplayStats of(ChestStorage storage, ItemStack type) {
        if (type.isEmpty()) {
            return EMPTY;
        }
        ItemStack stored = storage.getStoredType(type);
        long count = 0;
        long capacity = 0;
        boolean locked = false;
        boolean voided = false;
        for (int slot = 0; slot < storage.getSlotCount(); slot++) {
            DeepSlot deepSlot = storage.getSlot(slot);
            if (deepSlot.matches(stored)) {
                count = CapacityProfile.saturatedAdd(count, storage.getAvailable(slot, type));
                capacity = CapacityProfile.saturatedAdd(capacity, storage.getCapacity(type));
                locked |= deepSlot.isLocked();
                voided |= deepSlot.isVoiding();
            }
        }
        return new DisplayStats(count, capacity, locked, voided, storage.getFamily(type).isPresent());
    }

    /**
     * @return How full the slots holding the type are, from 0 to 1.
     */
    public float getFillLevel() {
        return capacity <= 0 ? 0 : (float) Math.min(1D, (double) count / capacity);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("count", count);
        tag.putLong("capacity", capacity);
        tag.putBoolean("locked", locked);
        tag.putBoolean("voided", voided);
        tag.putBoolean("compressed", compressed);
        return tag;
    }

    public static DisplayStats fromTag(CompoundTag tag) {
        return new DisplayStats(tag.getLong("count"), tag.getLong("capacity"), tag.getBoolean("locked"),
                tag.getBoolean("voided"), tag.getBoolean("compressed"));
    }
}
