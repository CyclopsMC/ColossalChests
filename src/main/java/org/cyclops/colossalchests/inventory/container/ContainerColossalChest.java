package org.cyclops.colossalchests.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.inventory.container.ExtendedInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;

/**
 * Container for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class ContainerColossalChest extends ExtendedInventoryContainer {

    private static final int INVENTORY_OFFSET_X = 28;
    private static final int INVENTORY_OFFSET_Y = 107;
    private static final int ARMOR_INVENTORY_OFFSET_X = 192;
    private static final int ARMOR_INVENTORY_OFFSET_Y = 109;

    private static final int CHEST_INVENTORY_OFFSET_X = 59;
    private static final int CHEST_INVENTORY_OFFSET_Y = 13;
    /**
     * Amount of rows in the chest.
     */
    public static final int CHEST_INVENTORY_ROWS = 5;
    /**
     * Amount of columns in the chest.
     */
    public static final int CHEST_INVENTORY_COLUMNS = 9;

    /**
     * Container slot X coordinate.
     */
    public static final int SLOT_CONTAINER_X = 6;
    /**
     * Container slot Y coordinate.
     */
    public static final int SLOT_CONTAINER_Y = 46;

    private static final int UPGRADE_INVENTORY_OFFSET_X = -22;
    private static final int UPGRADE_INVENTORY_OFFSET_Y = 6;

    private final TileColossalChest tile;

    /**
     * Make a new instance.
     * @param inventory The inventory of the player.
     * @param tile The tile entity that calls the GUI.
     */
    public ContainerColossalChest(InventoryPlayer inventory, TileColossalChest tile) {
        super(inventory, ColossalChest.getInstance());

        this.tile = tile;

        tile.openInventory(inventory.player);
        
        addChestSlots(CHEST_INVENTORY_ROWS, CHEST_INVENTORY_COLUMNS);

        this.addPlayerInventory(inventory, INVENTORY_OFFSET_X, INVENTORY_OFFSET_Y);
    }

    protected void addChestSlots(int rows, int columns) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                addSlotToContainer(makeSlot(tile, column + row * columns, CHEST_INVENTORY_OFFSET_X + column * 18, CHEST_INVENTORY_OFFSET_Y + row * 18));
            }
        }
    }

    protected Slot makeSlot(IInventory inventory, int index, int row, int column) {
        return new SlotExtended(inventory, index, row, column);
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        super.onContainerClosed(entityplayer);
        tile.closeInventory(entityplayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.canInteractWith(playerIn);
    }

    @Override
    protected int getSizeInventory() {
        return tile.getSizeInventory();
    }
}