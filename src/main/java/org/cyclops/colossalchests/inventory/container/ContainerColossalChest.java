package org.cyclops.colossalchests.inventory.container;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.network.packet.WindowItemsFragmentPacket;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Container for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class ContainerColossalChest extends ScrollingInventoryContainer<Slot> {

    private static final int INVENTORY_OFFSET_X = 9;
    private static final int INVENTORY_OFFSET_Y = 112;

    private static final int CHEST_INVENTORY_OFFSET_X = 9;
    private static final int CHEST_INVENTORY_OFFSET_Y = 18;
    /**
     * Amount of visible rows in the chest.
     */
    public static final int CHEST_INVENTORY_ROWS = 5;
    /**
     * Amount of columns in the chest.
     */
    public static final int CHEST_INVENTORY_COLUMNS = 9;

    private final TileColossalChest tile;
    private final List<Slot> chestSlots;

    /**
     * Make a new instance.
     * @param inventory The inventory of the player.
     * @param tile The tile entity that calls the GUI.
     */
    public ContainerColossalChest(InventoryPlayer inventory, TileColossalChest tile) {
        super(inventory, ColossalChest.getInstance(), Collections.<Slot>emptyList(), new IItemPredicate<Slot>() {
            @Override
            public boolean apply(Slot item, Pattern pattern) {
                return true;
            }
        });

        this.tile = tile;
        tile.openInventory(inventory.player);
        this.chestSlots = Lists.newArrayListWithCapacity(tile.getSizeInventory());
        this.addChestSlots(tile.getSizeInventory() / CHEST_INVENTORY_COLUMNS, CHEST_INVENTORY_COLUMNS);
        this.addPlayerInventory(inventory, INVENTORY_OFFSET_X, INVENTORY_OFFSET_Y);
        updateFilter("");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Slot> getUnfilteredItems() {
        return this.chestSlots;
    }

    protected void addChestSlots(int rows, int columns) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                Slot slot = makeSlot(tile, column + row * columns, CHEST_INVENTORY_OFFSET_X + column * 18, CHEST_INVENTORY_OFFSET_Y + row * 18);
                addSlotToContainer(slot);
                chestSlots.add(slot);
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

    @Override
    public int getColumns() {
        return CHEST_INVENTORY_COLUMNS;
    }

    @Override
    public int getPageSize() {
        return 5;
    }

    protected void disableSlot(int slotIndex) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xDisplayPosition = Integer.MIN_VALUE;
        slot.yDisplayPosition = Integer.MIN_VALUE;
    }

    protected void enableSlot(int slotIndex, int row, int column) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        slot.xDisplayPosition = CHEST_INVENTORY_OFFSET_X + column * 18;
        slot.yDisplayPosition = CHEST_INVENTORY_OFFSET_Y + row * 18;
    }

    @Override
    protected void onScroll() {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlot(i);
        }
    }

    @Override
    protected void enableElementAt(int visibleIndex, int elementIndex, Slot element) {
        super.enableElementAt(visibleIndex, elementIndex, element);
        int column = visibleIndex % getColumns();
        int row = (visibleIndex - column) / getColumns();
        enableSlot(elementIndex, row, column);
    }

    @Override
    public void onCraftGuiOpened(ICrafting listener) {
        if (this.crafters.contains(listener)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.crafters.add(listener);
            if(listener instanceof EntityPlayerMP) {
                updateCraftingInventory((EntityPlayerMP) listener, getInventory());
            } else {
                listener.updateCraftingInventory(this, this.getInventory());
            }
            this.detectAndSendChanges();
        }
    }

    // Modified from EntityPlayerMP#updateCraftingInventory
    public void updateCraftingInventory(EntityPlayerMP player, List<ItemStack> allItems) {
        int max = GeneralConfig.maxSlotsPerPacket;
        // Custom packet sending to be able to handle large inventories
        NetHandlerPlayServer playerNetServerHandler = player.playerNetServerHandler;
        // Modification of logic in EntityPlayerMP#updateCraftingInventory
        for(int i = 0; i < allItems.size(); i+= max) {
            List<ItemStack> items = allItems.subList(i, Math.min(allItems.size(), i + max));
            ColossalChests._instance.getPacketHandler().sendToPlayer(new WindowItemsFragmentPacket(windowId, i, items), player);
        }
        playerNetServerHandler.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
    }
}