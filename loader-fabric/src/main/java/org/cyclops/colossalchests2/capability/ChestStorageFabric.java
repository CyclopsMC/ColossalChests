package org.cyclops.colossalchests2.capability;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;

import java.util.Iterator;
import java.util.List;

/**
 * Fabric item storage on a chest storage, with true long counts and transaction support.
 * A transaction snapshots all slots, so an aborted transaction leaves the storage untouched.
 * @author rubensworks
 */
public class ChestStorageFabric extends SnapshotParticipant<DeepSlot[]> implements SlottedStorage<ItemVariant> {

    private final ChestStorage storage;

    public ChestStorageFabric(ChestStorage storage) {
        this.storage = storage;
    }

    public ChestStorage getStorage() {
        return storage;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack type = resource.toStack();
        long inserted = storage.insert(type, maxAmount, true);
        if (inserted > 0) {
            updateSnapshots(transaction);
            storage.insert(type, maxAmount, false);
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack type = resource.toStack();
        long extracted = storage.extract(type, maxAmount, true);
        if (extracted > 0) {
            updateSnapshots(transaction);
            storage.extract(type, maxAmount, false);
        }
        return extracted;
    }

    @Override
    public int getSlotCount() {
        return storage.getSlotCount();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return new SlotView(slot);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<StorageView<ItemVariant>> views = Lists.newArrayListWithCapacity(storage.getSlotCount());
        for (int slot = 0; slot < storage.getSlotCount(); slot++) {
            views.add(new SlotView(slot));
        }
        return views.iterator();
    }

    @Override
    protected DeepSlot[] createSnapshot() {
        return storage.snapshotSlots();
    }

    @Override
    protected void readSnapshot(DeepSlot[] snapshot) {
        storage.restoreSlots(snapshot);
    }

    /**
     * A single slot, sharing the snapshots of its parent.
     */
    private class SlotView implements SingleSlotStorage<ItemVariant> {

        private final int slot;

        private SlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            ItemStack type = resource.toStack();
            long inserted = storage.insert(slot, type, maxAmount, true);
            if (inserted > 0) {
                updateSnapshots(transaction);
                storage.insert(slot, type, maxAmount, false);
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (!storage.getSlot(slot).matches(resource.toStack())) {
                return 0;
            }
            long extracted = storage.extract(slot, maxAmount, true);
            if (extracted > 0) {
                updateSnapshots(transaction);
                storage.extract(slot, maxAmount, false);
            }
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return storage.getSlot(slot).getCount() == 0;
        }

        @Override
        public ItemVariant getResource() {
            DeepSlot deepSlot = storage.getSlot(slot);
            return deepSlot.getCount() == 0 ? ItemVariant.blank() : ItemVariant.of(deepSlot.getPrototype());
        }

        @Override
        public long getAmount() {
            return storage.getSlot(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return storage.getCapacity(slot);
        }
    }
}
