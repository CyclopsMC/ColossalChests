package org.cyclops.colossalchests2.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.jetbrains.annotations.Nullable;

/**
 * The settings of a Display wall: the item it shows.
 * @author rubensworks
 */
public class ContainerDisplay extends ContainerGhostSettings {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 136;
    public static final int DISPLAYED_X = 80;
    public static final int DISPLAYED_Y = 20;
    public static final int INVENTORY_Y = 54;

    /**
     * Client-side constructor.
     */
    public ContainerDisplay(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, new SimpleContainer(1), null);
        data.readBlockPos();
    }

    /**
     * Server-side constructor.
     */
    public ContainerDisplay(int id, Inventory inventory, BlockEntityChestWall wall) {
        this(id, inventory, wall.getDisplayedContainer(), wall);
    }

    private ContainerDisplay(int id, Inventory inventory, Container displayed, @Nullable BlockEntityChestWall wall) {
        super(RegistryEntries.MENU_DISPLAY.value(), id, wall);
        addGhostSlot(displayed, 0, DISPLAYED_X, DISPLAYED_Y);
        addPlayerInventory(inventory, INVENTORY_Y);
    }
}
