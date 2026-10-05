package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.jetbrains.annotations.Nullable;

/**
 * Settings of a wall in ghost slots, followed by the player inventory.
 * Ghost slots hold copies of what is clicked into them: the cursor is never taken.
 * @author rubensworks
 */
public abstract class ContainerGhostSettings extends AbstractContainerMenu {

    @Nullable
    protected final BlockEntityChestWall wall;
    private int ghostSlots;

    protected ContainerGhostSettings(@Nullable MenuType<?> type, int id, @Nullable BlockEntityChestWall wall) {
        super(type, id);
        this.wall = wall;
    }

    protected void addGhostSlot(Container container, int slot, int x, int y) {
        addSlot(new GhostSlot(container, slot, x, y));
        ghostSlots++;
    }

    protected void addPlayerInventory(Inventory inventory, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, y + 58));
        }
    }

    /**
     * @param slot A slot index.
     * @return If it is one of the ghost slots.
     */
    public boolean isGhostSlot(int slot) {
        return slot >= 0 && slot < ghostSlots;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isGhostSlot(slotId)) {
            // Clicking copies one of the cursor in, or clears with an empty cursor.
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                ItemStack carried = getCarried();
                getSlot(slotId).set(clickType == ClickType.PICKUP && !carried.isEmpty() ? carried.copyWithCount(1) : ItemStack.EMPTY);
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isGhostSlot(index)) {
            return ItemStack.EMPTY;
        }
        // Shift-clicking an item copies it into the first free ghost slot, or the only one, and leaves it where it is.
        ItemStack stack = getSlot(index).getItem();
        if (!stack.isEmpty()) {
            int free = -1;
            for (int i = 0; i < ghostSlots; i++) {
                ItemStack entry = getSlot(i).getItem();
                if (ItemStack.isSameItemSameComponents(entry, stack)) {
                    return ItemStack.EMPTY;
                }
                if (entry.isEmpty() && free < 0) {
                    free = i;
                }
            }
            if (free < 0 && ghostSlots == 1) {
                free = 0;
            }
            if (free >= 0) {
                getSlot(free).set(stack.copyWithCount(1));
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        return !(slot instanceof GhostSlot);
    }

    @Override
    public boolean stillValid(Player player) {
        return wall == null || (!wall.isRemoved() && Container.stillValidBlockEntity(wall, player));
    }

    /**
     * A settings slot, only changed through {@link #clicked(int, int, ClickType, Player)}.
     */
    public static class GhostSlot extends Slot {

        public GhostSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
