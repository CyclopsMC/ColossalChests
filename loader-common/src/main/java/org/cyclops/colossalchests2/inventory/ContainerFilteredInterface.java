package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.capability.WallAccess;
import org.jetbrains.annotations.Nullable;

/**
 * The settings of a Filtered Interface: filter slots, mode and extraction form.
 * Settings slots hold copies of what is clicked into them, like ghost slots.
 * @author rubensworks
 */
public class ContainerFilteredInterface extends AbstractContainerMenu {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;
    public static final int FILTER_Y = 18;
    public static final int FORM_X = 8;
    public static final int FORM_Y = 44;
    public static final int INVENTORY_Y = 84;
    public static final int BUTTON_MODE = 0;

    private static final int SETTINGS_SLOTS = BlockEntityChestWall.FILTER_SLOTS + 1;

    private final BlockPos pos;
    @Nullable
    private final BlockEntityChestWall wall;
    private final DataSlot mode;

    /**
     * Client-side constructor.
     */
    public ContainerFilteredInterface(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos(), new SimpleContainer(SETTINGS_SLOTS), null);
    }

    /**
     * Server-side constructor.
     */
    public ContainerFilteredInterface(int id, Inventory inventory, BlockEntityChestWall wall) {
        this(id, inventory, wall.getBlockPos(), wall.getSettings(), wall);
    }

    private ContainerFilteredInterface(int id, Inventory inventory, BlockPos pos, Container settings, @Nullable BlockEntityChestWall wall) {
        super(RegistryEntries.MENU_FILTERED_INTERFACE.value(), id);
        this.pos = pos;
        this.wall = wall;
        for (int i = 0; i < BlockEntityChestWall.FILTER_SLOTS; i++) {
            addSlot(new GhostSlot(settings, i, 8 + i * 18, FILTER_Y));
        }
        addSlot(new GhostSlot(settings, BlockEntityChestWall.FORM_SLOT, FORM_X, FORM_Y));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }
        this.mode = wall == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return wall.getMode().ordinal();
            }

            @Override
            public void set(int value) {
                wall.setMode(WallAccess.Mode.values()[value]);
            }
        };
        addDataSlot(this.mode);
    }

    public BlockPos getPos() {
        return pos;
    }

    public WallAccess.Mode getMode() {
        WallAccess.Mode[] modes = WallAccess.Mode.values();
        return modes[Math.floorMod(mode.get(), modes.length)];
    }

    /**
     * @param slot A slot index.
     * @return If it is one of the settings slots.
     */
    public static boolean isSettingsSlot(int slot) {
        return slot >= 0 && slot < SETTINGS_SLOTS;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_MODE && wall != null) {
            wall.setMode(wall.getMode().next());
            return true;
        }
        return false;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isSettingsSlot(slotId)) {
            // Ghost slots: the cursor is never taken, clicking copies one of it in, or clears with an empty cursor.
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
        if (isSettingsSlot(index)) {
            return ItemStack.EMPTY;
        }
        // Shift-clicking an item adds it to the filter, the item stays where it is.
        ItemStack stack = getSlot(index).getItem();
        if (!stack.isEmpty()) {
            int free = -1;
            for (int i = 0; i < BlockEntityChestWall.FILTER_SLOTS; i++) {
                ItemStack entry = getSlot(i).getItem();
                if (ItemStack.isSameItemSameComponents(entry, stack)) {
                    return ItemStack.EMPTY;
                }
                if (entry.isEmpty() && free < 0) {
                    free = i;
                }
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
