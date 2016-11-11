package org.cyclops.colossalchests.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketSetSlot;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.block.ColossalChest;
import org.cyclops.colossalchests.network.packet.WindowItemsFragmentPacket;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Container for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
@ChestContainer(isLargeChest = true)
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
    private int lastInventoryHash = -2;

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
    public void addListener(IContainerListener listener) {
        if (this.listeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.listeners.add(listener);
            if(listener instanceof EntityPlayerMP) {
                updateCraftingInventory((EntityPlayerMP) listener, getInventory());
            } else {
                listener.updateCraftingInventory(this, this.getInventory());
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    public void detectAndSendChanges() {
        int newHash = tile.getInventoryHash();
        if (lastInventoryHash != newHash) {
            lastInventoryHash = newHash;
            super.detectAndSendChanges();
        }
    }

    protected int getTagSize(NBTBase tag) {
        if (tag instanceof NBTBase.NBTPrimitive || tag instanceof NBTTagEnd) {
            return 1;
        }
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) tag;
            int size = 0;
            for (String key : compound.getKeySet()) {
                size += getTagSize(compound.getTag(key));
            }
            return size;
        }
        if (tag instanceof NBTTagByteArray) {
            return ((NBTTagByteArray) tag).getByteArray().length;
        }
        if (tag instanceof NBTTagIntArray) {
            return ((NBTTagIntArray) tag).getIntArray().length * 32;
        }
        if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            int size = 0;
            for (int i = 0; i < list.tagCount(); i++) {
                size += getTagSize(list.get(i));
            }
            return size;
        }
        if (tag instanceof NBTTagString) {
            try {
                return ((NBTTagString) tag).getString().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {}
        }
        return tag.toString().length();
    }

    // Modified from EntityPlayerMP#updateCraftingInventory
    public void updateCraftingInventory(EntityPlayerMP player, List<ItemStack> allItems) {
        int maxBufferSize = GeneralConfig.maxPacketBufferSize;
        // Custom packet sending to be able to handle large inventories
        NetHandlerPlayServer playerNetServerHandler = player.connection;
        // Modification of logic in EntityPlayerMP#updateCraftingInventory
        NBTTagCompound sendBuffer = new NBTTagCompound();
        NBTTagList sendList = new NBTTagList();
        sendBuffer.setTag("stacks", sendList);
        int i = 0;
        int bufferSize = 0;
        int sent = 0;
        for (ItemStack itemStack : allItems) {
            if (itemStack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("slot", i);
                tag.setTag("stack", itemStack.serializeNBT());
                int tagSize = getTagSize(tag);
                if (bufferSize + tagSize + 100 < maxBufferSize) {
                    sendList.appendTag(tag);
                    bufferSize += tagSize;
                } else {
                    // Flush
                    ColossalChests._instance.getPacketHandler().sendToPlayer(new WindowItemsFragmentPacket(windowId, sendBuffer), player);
                    sendBuffer = new NBTTagCompound();
                    sendList = new NBTTagList();
                    sendList.appendTag(tag);
                    sendBuffer.setTag("stacks", sendList);
                    bufferSize = tagSize;
                }
            }
            i++;
        }
        if (sendList.tagCount() > 0) {
            // Flush
            ColossalChests._instance.getPacketHandler().sendToPlayer(new WindowItemsFragmentPacket(windowId, sendBuffer), player);
        }
        playerNetServerHandler.sendPacket(new SPacketSetSlot(-1, -1, player.inventory.getItemStack()));
    }

    /**
     * @return Container selection options for inventory tweaks.
     */
    @ContainerSectionCallback
    public Map<ContainerSection, List<Slot>> getContainerSelection() {
        Map<ContainerSection, List<Slot>> selection = Maps.newHashMap();
        List<Slot> chest = Lists.newArrayList();
        List<Slot> playerInventory = Lists.newArrayList();
        for(int i = 0; i < getSizeInventory(); i++) {
            chest.add(this.getSlot(i));
        }
        for(int i = getSizeInventory(); i < getSizeInventory() + 4 * 9; i++) {
            playerInventory.add(this.getSlot(i));
        }
        selection.put(ContainerSection.CHEST, chest);
        selection.put(ContainerSection.INVENTORY, playerInventory);
        return selection;
    }
}