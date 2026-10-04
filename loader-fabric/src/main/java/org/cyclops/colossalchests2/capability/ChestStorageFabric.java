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
 * Views for walls share the snapshots of the core's storage, so one transaction through several walls rolls back correctly.
 * @author rubensworks
 */
public class ChestStorageFabric extends SnapshotParticipant<DeepSlot[]> implements SlottedStorage<ItemVariant> {

    private final ChestStorage storage;
    private final ItemHandlerLogic logic;
    private final SnapshotParticipant<DeepSlot[]> snapshots;

    public ChestStorageFabric(ChestStorage storage) {
        this.storage = storage;
        this.logic = new ItemHandlerLogic(storage);
        this.snapshots = this;
    }

    private ChestStorageFabric(ChestStorageFabric parent, WallAccess access) {
        this.storage = parent.storage;
        this.logic = new ItemHandlerLogic(storage, access);
        this.snapshots = parent;
    }

    /**
     * @param access What automation may do through the view.
     * @return A view on the same storage, limited by the access rules.
     */
    public ChestStorageFabric withAccess(WallAccess access) {
        return new ChestStorageFabric(this, access);
    }

    public ChestStorage getStorage() {
        return storage;
    }

    private WallAccess access() {
        return logic.getAccess();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack type = resource.toStack();
        if (!access().canInsert(storage, type)) {
            return 0;
        }
        long inserted = storage.insertAutomated(type, maxAmount, true, access().voidFull());
        if (inserted > 0) {
            snapshots.updateSnapshots(transaction);
            storage.insertAutomated(type, maxAmount, false, access().voidFull());
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        ItemStack type = resource.toStack();
        if (!access().canExtract(storage, type)) {
            return 0;
        }
        long extracted = storage.extract(type, maxAmount, true);
        if (extracted > 0) {
            snapshots.updateSnapshots(transaction);
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
            if (!access().canInsert(storage, type)) {
                return 0;
            }
            long inserted = storage.insertAutomated(slot, type, maxAmount, true, access().voidFull());
            if (inserted > 0) {
                snapshots.updateSnapshots(transaction);
                storage.insertAutomated(slot, type, maxAmount, false, access().voidFull());
            }
            return inserted;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            // Any form of a compressed slot's family can be extracted.
            ItemStack type = resource.toStack();
            if (!access().canExtract(storage, type)) {
                return 0;
            }
            long extracted = storage.extract(slot, type, maxAmount, true);
            if (extracted > 0) {
                snapshots.updateSnapshots(transaction);
                storage.extract(slot, type, maxAmount, false);
            }
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return getAmount() == 0;
        }

        @Override
        public ItemVariant getResource() {
            return getAmount() == 0 ? ItemVariant.blank() : ItemVariant.of(logic.getExtractionType(slot));
        }

        @Override
        public long getAmount() {
            ItemStack type = logic.getExtractionType(slot);
            return type.isEmpty() ? 0 : storage.getAvailable(slot, type);
        }

        @Override
        public long getCapacity() {
            ItemStack type = logic.getExtractionType(slot);
            return type.isEmpty() ? storage.getCapacity(slot) : storage.getCapacity(type);
        }
    }
}
