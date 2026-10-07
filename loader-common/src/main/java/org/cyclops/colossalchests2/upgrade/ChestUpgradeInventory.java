package org.cyclops.colossalchests2.upgrade;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.api.upgrade.ChestUpgrade;

import java.util.List;

/**
 * The upgrade slots of a core. Each slot holds one upgrade item.
 * What may be inserted or removed is decided by the {@link Owner}.
 * @author rubensworks
 */
public class ChestUpgradeInventory implements Container {

    private final Owner owner;
    private NonNullList<ItemStack> items;

    public ChestUpgradeInventory(Owner owner, int size) {
        this.owner = owner;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public UpgradeSet getUpgradeSet() {
        return UpgradeSet.of(items);
    }

    /**
     * Change the slot count. Items in slots beyond the new count are kept by growing to fit them.
     * @param size The wanted slot count.
     */
    public void resize(int size) {
        int highest = -1;
        for (int slot = 0; slot < items.size(); slot++) {
            if (!items.get(slot).isEmpty()) {
                highest = slot;
            }
        }
        int newSize = Math.max(size, highest + 1);
        if (newSize != items.size()) {
            NonNullList<ItemStack> newItems = NonNullList.withSize(newSize, ItemStack.EMPTY);
            for (int slot = 0; slot < Math.min(newSize, items.size()); slot++) {
                newItems.set(slot, items.get(slot));
            }
            items = newItems;
        }
    }

    /**
     * Replace all items without validation, for loading.
     * @param stacks The new items, the inventory grows to fit them.
     */
    public void load(List<ItemStack> stacks) {
        if (stacks.size() > items.size()) {
            items = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
        } else {
            items.replaceAll(stack -> ItemStack.EMPTY);
        }
        for (int slot = 0; slot < stacks.size(); slot++) {
            items.set(slot, stacks.get(slot).copy());
        }
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        owner.onUpgradesChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(stack);
        return upgrade != null && items.get(slot).isEmpty() && owner.canAddUpgrade(slot, upgrade);
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        // Automation never removes upgrades.
        return false;
    }

    @Override
    public void clearContent() {
        items.replaceAll(stack -> ItemStack.EMPTY);
        setChanged();
    }

    /**
     * Decides what the upgrade slots accept, and applies changes.
     */
    public interface Owner {

        /**
         * @param slot The target slot.
         * @param upgrade An upgrade to insert.
         * @return If the chest takes one more of the upgrade in that slot.
         */
        boolean canAddUpgrade(int slot, ChestUpgrade upgrade);

        /**
         * Called after the upgrade slots changed.
         */
        void onUpgradesChanged();

    }

}
