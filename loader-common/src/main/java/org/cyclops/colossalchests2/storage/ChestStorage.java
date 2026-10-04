package org.cyclops.colossalchests2.storage;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;

/**
 * Deep-slot storage engine: a fixed number of slots, each holding one item type with a long count.
 * Capacity is determined by a {@link CapacityProfile}.
 * Slots holding more than their capacity are extract-only.
 * This ensures that lowering capacity never deletes items.
 * @author rubensworks
 */
public class ChestStorage {

    public static final Codec<Contents> CODEC = Contents.CODEC;

    private DeepSlot[] slots;
    private CapacityProfile profile;
    private final BitSet dirty = new BitSet();
    private final List<IntConsumer> listeners = Lists.newArrayList();
    private int state;

    public ChestStorage(int slotCount, CapacityProfile profile) {
        if (slotCount < 0) {
            throw new IllegalArgumentException("Negative slot count: " + slotCount);
        }
        this.slots = new DeepSlot[slotCount];
        Arrays.fill(this.slots, DeepSlot.EMPTY);
        this.profile = profile;
    }

    public int getSlotCount() {
        return slots.length;
    }

    public CapacityProfile getProfile() {
        return profile;
    }

    public DeepSlot getSlot(int slot) {
        return slots[slot];
    }

    /**
     * @return A value that changes whenever the contents or capacities change.
     */
    public int getState() {
        return state;
    }

    /**
     * @param type An item type.
     * @return The capacity of any slot for the given type.
     */
    public long getCapacity(ItemStack type) {
        return profile.capacityFor(type.getMaxStackSize());
    }

    /**
     * @param slot A slot index.
     * @return The capacity of the slot for its current type, or for a full-stack type if empty.
     */
    public long getCapacity(int slot) {
        DeepSlot deepSlot = slots[slot];
        return deepSlot.isEmpty() ? profile.capacityFor(Item.DEFAULT_MAX_STACK_SIZE) : getCapacity(deepSlot.getPrototype());
    }

    /**
     * @param slot A slot index.
     * @return If the slot holds more than its capacity, so it only allows extraction.
     */
    public boolean isExtractOnly(int slot) {
        DeepSlot deepSlot = slots[slot];
        return !deepSlot.isEmpty() && deepSlot.getCount() > getCapacity(deepSlot.getPrototype());
    }

    /**
     * @param slot A slot index.
     * @param type An item type.
     * @return If the type may go into the slot, ignoring how full it is.
     */
    public boolean canAccept(int slot, ItemStack type) {
        if (type.isEmpty() || getCapacity(type) <= 0) {
            return false;
        }
        DeepSlot deepSlot = slots[slot];
        return deepSlot.isEmpty() || deepSlot.matches(type);
    }

    /**
     * @param type An item type.
     * @return If an existing slot holds this type or a free slot could take it.
     */
    public boolean canFit(ItemStack type) {
        return insert(type, 1, true) == 1;
    }

    /**
     * Insert into a specific slot.
     * @param slot A slot index.
     * @param type The item type, its count is ignored.
     * @param amount The amount to insert.
     * @param simulate If the storage must not change.
     * @return The amount that was (or would be) inserted.
     */
    public long insert(int slot, ItemStack type, long amount, boolean simulate) {
        if (amount <= 0 || !canAccept(slot, type)) {
            return 0;
        }
        DeepSlot deepSlot = slots[slot];
        long space = Math.max(0, getCapacity(type) - deepSlot.getCount());
        long inserted = Math.min(amount, space);
        if (!simulate && inserted > 0) {
            setSlot(slot, deepSlot.isEmpty() ? DeepSlot.of(type, inserted) : deepSlot.withCount(deepSlot.getCount() + inserted));
        }
        return inserted;
    }

    /**
     * Insert anywhere: first into slots of the same type, then into free unlocked slots.
     * @param type The item type, its count is ignored.
     * @param amount The amount to insert.
     * @param simulate If the storage must not change.
     * @return The amount that was (or would be) inserted.
     */
    public long insert(ItemStack type, long amount, boolean simulate) {
        long remaining = amount;
        for (int slot = 0; slot < slots.length && remaining > 0; slot++) {
            if (slots[slot].matches(type)) {
                remaining -= insert(slot, type, remaining, simulate);
            }
        }
        for (int slot = 0; slot < slots.length && remaining > 0; slot++) {
            if (slots[slot].isEmpty()) {
                remaining -= insert(slot, type, remaining, simulate);
            }
        }
        return amount - remaining;
    }

    /**
     * Extract from a specific slot, also from extract-only slots.
     * @param slot A slot index.
     * @param amount The maximum amount to extract.
     * @param simulate If the storage must not change.
     * @return The amount that was (or would be) extracted.
     */
    public long extract(int slot, long amount, boolean simulate) {
        DeepSlot deepSlot = slots[slot];
        long extracted = Math.min(Math.max(0, amount), deepSlot.getCount());
        if (!simulate && extracted > 0) {
            setSlot(slot, deepSlot.withCount(deepSlot.getCount() - extracted));
        }
        return extracted;
    }

    /**
     * Extract a type from any slot holding it.
     * @param type The item type.
     * @param amount The maximum amount to extract.
     * @param simulate If the storage must not change.
     * @return The amount that was (or would be) extracted.
     */
    public long extract(ItemStack type, long amount, boolean simulate) {
        long remaining = amount;
        for (int slot = 0; slot < slots.length && remaining > 0; slot++) {
            if (slots[slot].matches(type)) {
                remaining -= extract(slot, remaining, simulate);
            }
        }
        return amount - remaining;
    }

    /**
     * Lock or unlock a slot. Empty slots without a type cannot be locked.
     * @param slot A slot index.
     * @param locked The new lock state.
     * @return If the slot now has the requested lock state.
     */
    public boolean setLocked(int slot, boolean locked) {
        DeepSlot deepSlot = slots[slot];
        if (deepSlot.isEmpty()) {
            return !locked;
        }
        if (deepSlot.isLocked() != locked) {
            setSlot(slot, deepSlot.withLocked(locked));
        }
        return true;
    }

    /**
     * Reserve an empty slot for a type at zero count, or lock a slot already holding that type.
     * @param slot A slot index.
     * @param type The item type to reserve.
     * @return If the slot was locked to the type.
     */
    public boolean lockTo(int slot, ItemStack type) {
        DeepSlot deepSlot = slots[slot];
        if (type.isEmpty()) {
            return false;
        }
        if (deepSlot.matches(type)) {
            return setLocked(slot, true);
        }
        if (deepSlot.getCount() > 0) {
            return false;
        }
        setSlot(slot, DeepSlot.of(type, 0, true, null));
        return true;
    }

    /**
     * Lock all slots that hold items.
     * @return The number of newly locked slots.
     */
    public int lockAllFilled() {
        int count = 0;
        for (int slot = 0; slot < slots.length; slot++) {
            if (!slots[slot].isEmpty() && !slots[slot].isLocked()) {
                setLocked(slot, true);
                count++;
            }
        }
        return count;
    }

    /**
     * Unlock all slots. Contents are untouched, empty reserved slots become free.
     */
    public void clearLocks() {
        for (int slot = 0; slot < slots.length; slot++) {
            if (slots[slot].isLocked()) {
                setLocked(slot, false);
            }
        }
    }

    /**
     * Mark or unmark a slot as voiding. Empty slots without a type cannot be marked.
     * @param slot A slot index.
     * @param voiding The new state.
     * @return If the slot now has the requested state.
     */
    public boolean setVoiding(int slot, boolean voiding) {
        DeepSlot deepSlot = slots[slot];
        if (deepSlot.isEmpty()) {
            return !voiding;
        }
        if (deepSlot.isVoiding() != voiding) {
            setSlot(slot, deepSlot.withVoiding(voiding));
        }
        return true;
    }

    /**
     * Mark all slots that hold items as voiding.
     * @return The number of newly marked slots.
     */
    public int voidAllFilled() {
        int count = 0;
        for (int slot = 0; slot < slots.length; slot++) {
            if (!slots[slot].isEmpty() && !slots[slot].isVoiding()) {
                setVoiding(slot, true);
                count++;
            }
        }
        return count;
    }

    /**
     * Unmark all voiding slots.
     */
    public void clearVoids() {
        for (int slot = 0; slot < slots.length; slot++) {
            if (slots[slot].isVoiding()) {
                setVoiding(slot, false);
            }
        }
    }

    /**
     * @param type An item type.
     * @return If a voiding slot holds the type.
     */
    public boolean isVoided(ItemStack type) {
        for (DeepSlot deepSlot : slots) {
            if (deepSlot.isVoiding() && deepSlot.matches(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Insert from automation. A type held by a voiding slot only fills the slots that already hold it, and what does
     * not fit is destroyed, so voided overflow never takes new slots. Player inserts never void.
     * @param type The item type, its count is ignored.
     * @param amount The amount to insert.
     * @param simulate If the storage must not change.
     * @return The amount accepted, including what is destroyed.
     */
    public long insertAutomated(ItemStack type, long amount, boolean simulate) {
        if (!isVoided(type)) {
            return insert(type, amount, simulate);
        }
        insertIntoSlotsHolding(type, amount, simulate);
        return amount;
    }

    /**
     * Insert from automation into a specific slot. What does not fit is destroyed if the slot is voiding for the type
     * and no other slot holding the type has room, so automation walking the slots does not void what fits elsewhere.
     * @param slot A slot index.
     * @param type The item type, its count is ignored.
     * @param amount The amount to insert.
     * @param simulate If the storage must not change.
     * @return The amount accepted, including what is destroyed.
     */
    public long insertAutomated(int slot, ItemStack type, long amount, boolean simulate) {
        long inserted = insert(slot, type, amount, simulate);
        DeepSlot deepSlot = slots[slot];
        if (inserted < amount && deepSlot.isVoiding() && deepSlot.matches(type)
                && insertIntoSlotsHolding(type, amount - inserted, true) == 0) {
            return amount;
        }
        return inserted;
    }

    private long insertIntoSlotsHolding(ItemStack type, long amount, boolean simulate) {
        long inserted = 0;
        for (int slot = 0; slot < slots.length && inserted < amount; slot++) {
            if (slots[slot].matches(type)) {
                inserted += insert(slot, type, amount - inserted, simulate);
            }
        }
        return inserted;
    }

    /**
     * @param slot A slot index.
     * @param form The item of the compression form to extract in, or null for the default.
     */
    public void setCompressionForm(int slot, @Nullable Item form) {
        DeepSlot deepSlot = slots[slot];
        if (!deepSlot.isEmpty() && !deepSlot.getCompressionForm().equals(Optional.ofNullable(form))) {
            setSlot(slot, deepSlot.withCompressionForm(form));
        }
    }

    /**
     * @param slotCount A new slot count.
     * @return Filled slots that would be dropped.
     */
    public ResizeResult validateSlotCount(int slotCount) {
        List<Integer> offending = Lists.newArrayList();
        for (int slot = Math.max(0, slotCount); slot < slots.length; slot++) {
            if (slots[slot].getCount() > 0) {
                offending.add(slot);
            }
        }
        return ResizeResult.of(offending);
    }

    /**
     * Change the slot count if no filled slot would be dropped.
     * @param slotCount A new slot count.
     * @return The validation result, the storage is unchanged if not ok.
     */
    public ResizeResult setSlotCount(int slotCount) {
        ResizeResult result = validateSlotCount(slotCount);
        if (result.isOk() && slotCount != slots.length) {
            int oldCount = slots.length;
            slots = Arrays.copyOf(slots, slotCount);
            for (int slot = oldCount; slot < slotCount; slot++) {
                slots[slot] = DeepSlot.EMPTY;
            }
            dirty.clear(slotCount, Math.max(slotCount, oldCount));
            changed(-1);
        }
        return result;
    }

    /**
     * @param newProfile A new capacity profile.
     * @return Slots whose contents would exceed their new capacity.
     */
    public ResizeResult validateProfile(CapacityProfile newProfile) {
        List<Integer> offending = Lists.newArrayList();
        for (int slot = 0; slot < slots.length; slot++) {
            DeepSlot deepSlot = slots[slot];
            if (!deepSlot.isEmpty() && deepSlot.getCount() > newProfile.capacityFor(deepSlot.getPrototype().getMaxStackSize())) {
                offending.add(slot);
            }
        }
        return ResizeResult.of(offending);
    }

    /**
     * Change the capacity profile if all contents still fit.
     * @param newProfile A new capacity profile.
     * @return The validation result, the storage is unchanged if not ok.
     */
    public ResizeResult setProfile(CapacityProfile newProfile) {
        ResizeResult result = validateProfile(newProfile);
        if (result.isOk()) {
            forceProfile(newProfile);
        }
        return result;
    }

    /**
     * Change the capacity profile unconditionally, used when the structure re-forms smaller or
     * config values were lowered. Over-capacity slots become extract-only.
     * @param newProfile A new capacity profile.
     * @return The slots that are now extract-only.
     */
    public ResizeResult forceProfile(CapacityProfile newProfile) {
        ResizeResult result = validateProfile(newProfile);
        if (!newProfile.equals(this.profile)) {
            this.profile = newProfile;
            changed(-1);
        }
        return result;
    }

    /**
     * @param listener Called with a slot index after a slot changed, or -1 after the layout or capacity changed.
     */
    public void addListener(IntConsumer listener) {
        listeners.add(listener);
    }

    public void removeListener(IntConsumer listener) {
        listeners.remove(listener);
    }

    /**
     * @return If slots changed since the last {@link #drainDirtySlots()}.
     */
    public boolean hasDirtySlots() {
        return !dirty.isEmpty();
    }

    /**
     * @return The slots that changed since the last call, in ascending order.
     */
    public int[] drainDirtySlots() {
        int[] result = dirty.stream().toArray();
        dirty.clear();
        return result;
    }

    /**
     * Mark all slots dirty, for a full resync.
     */
    public void markAllDirty() {
        dirty.set(0, slots.length);
    }

    /**
     * @return A copy of all slots, to roll back to with {@link #restoreSlots(DeepSlot[])}.
     */
    public DeepSlot[] snapshotSlots() {
        return slots.clone();
    }

    /**
     * Roll back to a snapshot of the same slot count. Restored slots count as changes.
     * @param snapshot A result of {@link #snapshotSlots()}.
     */
    public void restoreSlots(DeepSlot[] snapshot) {
        if (snapshot.length != slots.length) {
            throw new IllegalArgumentException("Snapshot has " + snapshot.length + " slots, storage has " + slots.length);
        }
        for (int slot = 0; slot < snapshot.length; slot++) {
            setSlot(slot, snapshot[slot]);
        }
    }

    protected void setSlot(int slot, DeepSlot deepSlot) {
        if (!slots[slot].equals(deepSlot)) {
            slots[slot] = deepSlot;
            dirty.set(slot);
            changed(slot);
        }
    }

    protected void changed(int slot) {
        state++;
        for (IntConsumer listener : listeners) {
            listener.accept(slot);
        }
    }

    /**
     * @return A sparse snapshot of the stored contents, for serialization.
     */
    public Contents toContents() {
        List<Contents.Entry> entries = Lists.newArrayList();
        for (int slot = 0; slot < slots.length; slot++) {
            DeepSlot deepSlot = slots[slot];
            if (!deepSlot.isEmpty()) {
                entries.add(new Contents.Entry(slot, deepSlot.getPrototype(), deepSlot.getCount(),
                        deepSlot.isLocked(), deepSlot.isVoiding(), deepSlot.getCompressionForm()));
            }
        }
        return new Contents(slots.length, entries);
    }

    /**
     * Replace all contents. The slot count grows if entries lie beyond it, so nothing is ever dropped.
     * @param contents A snapshot.
     */
    public void loadContents(Contents contents) {
        int slotCount = contents.slotCount();
        for (Contents.Entry entry : contents.entries()) {
            slotCount = Math.max(slotCount, entry.slot() + 1);
        }
        slots = new DeepSlot[slotCount];
        Arrays.fill(slots, DeepSlot.EMPTY);
        for (Contents.Entry entry : contents.entries()) {
            slots[entry.slot()] = DeepSlot.of(entry.item(), entry.count(), entry.locked(), entry.voiding(), entry.form().orElse(null));
        }
        dirty.clear();
        markAllDirty();
        changed(-1);
    }

    /**
     * Sparse serialized form: only filled or reserved slots are written.
     * Entries that cannot be decoded, for example because their item's mod was removed, are skipped
     * instead of failing the whole storage.
     * @param slotCount The slot count.
     * @param entries The non-empty slots.
     */
    public record Contents(int slotCount, List<Entry> entries) {

        private static final Codec<List<Entry>> CODEC_ENTRIES = new Codec<>() {
            @Override
            public <T> DataResult<Pair<List<Entry>, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getList(input).map(elements -> {
                    List<Entry> entries = Lists.newArrayList();
                    elements.accept(element -> Entry.CODEC.parse(ops, element).ifSuccess(entries::add));
                    return Pair.of(entries, input);
                });
            }

            @Override
            public <T> DataResult<T> encode(List<Entry> input, DynamicOps<T> ops, T prefix) {
                return Entry.CODEC.listOf().encode(input, ops, prefix);
            }
        };

        public static final Codec<Contents> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(0, Integer.MAX_VALUE).fieldOf("slot_count").forGetter(Contents::slotCount),
                CODEC_ENTRIES.optionalFieldOf("slots", List.of()).forGetter(Contents::entries)
        ).apply(i, Contents::new));

        public record Entry(int slot, ItemStack item, long count, boolean locked, boolean voiding, Optional<Item> form) {
            // Unknown form items decode as no form, which falls back to the default form.
            private static final Codec<Optional<Item>> CODEC_FORM = ResourceLocation.CODEC.xmap(
                    BuiltInRegistries.ITEM::getOptional,
                    form -> BuiltInRegistries.ITEM.getKey(form.orElseThrow()));

            public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.intRange(0, Integer.MAX_VALUE).fieldOf("slot").forGetter(Entry::slot),
                    ItemStack.SINGLE_ITEM_CODEC.fieldOf("item").forGetter(Entry::item),
                    Codec.LONG.fieldOf("count").forGetter(Entry::count),
                    Codec.BOOL.optionalFieldOf("locked", false).forGetter(Entry::locked),
                    Codec.BOOL.optionalFieldOf("voiding", false).forGetter(Entry::voiding),
                    CODEC_FORM.optionalFieldOf("form", Optional.empty()).forGetter(Entry::form)
            ).apply(i, Entry::new));
        }
    }

}
