package org.cyclops.colossalchests.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NumberNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.NonNullList;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.network.packet.SetSlotLarge;
import org.cyclops.colossalchests.network.packet.WindowItemsFragmentPacket;
import org.cyclops.cyclopscore.inventory.LargeInventory;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.inventory.container.ScrollingInventoryContainer;
import org.cyclops.cyclopscore.inventory.slot.SlotExtended;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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


    private final List<Slot> chestSlots;
    private final NonNullList<ItemStack> inventoryItemStacks;
    private int lastInventoryState = -2;
    private boolean firstDetectionCheck = true;

    public ContainerColossalChest(int id, PlayerInventory playerInventory, PacketBuffer data) {
        this(id, playerInventory, new LargeInventory(data.readInt(), 64));
    }

    public ContainerColossalChest(int id, PlayerInventory playerInventory, IInventory inventory) {
        super(RegistryEntries.CONTAINER_COLOSSAL_CHEST, id, playerInventory, inventory, Collections.<Slot>emptyList(), (item, pattern) -> true);

        this.chestSlots = Lists.newArrayListWithCapacity(getSizeInventory());
        this.inventoryItemStacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        this.addChestSlots(getSizeInventory() / CHEST_INVENTORY_COLUMNS, CHEST_INVENTORY_COLUMNS);
        this.addPlayerInventory(playerInventory, INVENTORY_OFFSET_X, INVENTORY_OFFSET_Y);
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
                Slot slot = makeSlot(inventory, column + row * columns, CHEST_INVENTORY_OFFSET_X + column * 18, CHEST_INVENTORY_OFFSET_Y + row * 18);
                addSlot(slot);
                chestSlots.add(slot);
            }
        }
    }

    protected Slot makeSlot(IInventory inventory, int index, int row, int column) {
        return new SlotExtended(inventory, index, row, column);
    }

    @Override
    public int getColumns() {
        return CHEST_INVENTORY_COLUMNS;
    }

    @Override
    public int getPageSize() {
        return CHEST_INVENTORY_ROWS;
    }

    protected void disableSlot(int slotIndex) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        setSlotPosX(slot, Integer.MIN_VALUE);
        setSlotPosY(slot, Integer.MIN_VALUE);
    }

    protected void enableSlot(int slotIndex, int row, int column) {
        Slot slot = getSlot(slotIndex);
        // Yes I know this is ugly.
        // If you are reading this and know a better way, please tell me.
        setSlotPosX(slot, CHEST_INVENTORY_OFFSET_X + column * 18);
        setSlotPosY(slot, CHEST_INVENTORY_OFFSET_Y + row * 18);
    }

    @Override
    public void onScroll(int firstRow) {
        for(int i = 0; i < getUnfilteredItemCount(); i++) {
            disableSlot(i);
        }
        super.onScroll(firstRow);
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
            if(listener instanceof ServerPlayerEntity) {
                updateCraftingInventory((ServerPlayerEntity) listener, getInventory());
            } else {
                listener.sendAllContents(this, this.getInventory());
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    public void detectAndSendChanges() {
        int newState = ((SimpleInventory) inventory).getState();
        if (lastInventoryState != newState) {
            lastInventoryState = newState;
            detectAndSendChangesOverride();
        }
    }

    // Custom implementation of Container#detectAndSendChanges
    protected void detectAndSendChangesOverride() {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                if (!firstDetectionCheck) {
                    for (int j = 0; j < this.listeners.size(); ++j) {
                        IContainerListener listener = this.listeners.get(j);
                        if (listener instanceof ServerPlayerEntity) {
                            sendSlotContentsToPlayer((ServerPlayerEntity) listener, this, i, itemstack1);
                        } else {
                            listener.sendSlotContents(this, i, itemstack1);
                        }
                    }
                }
            }
        }
        firstDetectionCheck = false;
    }

    // Adapted from EntityPlayerMP#sendSlotContents
    protected void sendSlotContentsToPlayer(ServerPlayerEntity player, Container containerToSend, int slotInd, ItemStack stack) {
        if (!(containerToSend.getSlot(slotInd) instanceof CraftingResultSlot)) {
            if (!player.isChangingQuantityOnly) {
                ColossalChests._instance.getPacketHandler().sendToPlayer(
                        new SetSlotLarge(containerToSend.windowId, slotInd, stack), player);
            }
        }
    }

    protected int getTagSize(INBT tag) {
        if (tag instanceof NumberNBT || tag instanceof EndNBT) {
            return 1;
        }
        if (tag instanceof CompoundNBT) {
            CompoundNBT compound = (CompoundNBT) tag;
            int size = 0;
            for (String key : compound.keySet()) {
                size += getTagSize(compound.get(key));
            }
            return size;
        }
        if (tag instanceof ByteArrayNBT) {
            return ((ByteArrayNBT) tag).getByteArray().length;
        }
        if (tag instanceof IntArrayNBT) {
            return ((IntArrayNBT) tag).getIntArray().length * 32;
        }
        if (tag instanceof ListNBT) {
            ListNBT list = (ListNBT) tag;
            int size = 0;
            for (int i = 0; i < list.size(); i++) {
                size += getTagSize(list.get(i));
            }
            return size;
        }
        if (tag instanceof StringNBT) {
            try {
                return ((StringNBT) tag).getString().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {}
        }
        return tag.toString().length();
    }

    // Modified from EntityPlayerMP#updateCraftingInventory
    public void updateCraftingInventory(ServerPlayerEntity player, List<ItemStack> allItems) {
        int maxBufferSize = GeneralConfig.maxPacketBufferSize;
        // Custom packet sending to be able to handle large inventories
        ServerPlayNetHandler playerNetServerHandler = player.connection;
        // Modification of logic in EntityPlayerMP#updateCraftingInventory
        CompoundNBT sendBuffer = new CompoundNBT();
        ListNBT sendList = new ListNBT();
        sendBuffer.put("stacks", sendList);
        int i = 0;
        int bufferSize = 0;
        int sent = 0;
        for (ItemStack itemStack : allItems) {
            if (itemStack != null) {
                CompoundNBT tag = new CompoundNBT();
                tag.putInt("slot", i);
                tag.put("stack", itemStack.serializeNBT());
                int tagSize = getTagSize(tag);
                if (bufferSize + tagSize + 100 < maxBufferSize) {
                    sendList.add(tag);
                    bufferSize += tagSize;
                } else {
                    // Flush
                    ColossalChests._instance.getPacketHandler().sendToPlayer(new WindowItemsFragmentPacket(windowId, sendBuffer), player);
                    sendBuffer = new CompoundNBT();
                    sendList = new ListNBT();
                    sendList.add(tag);
                    sendBuffer.put("stacks", sendList);
                    bufferSize = tagSize;
                }
            }
            i++;
        }
        if (sendList.size() > 0) {
            // Flush
            ColossalChests._instance.getPacketHandler().sendToPlayer(new WindowItemsFragmentPacket(windowId, sendBuffer), player);
        }
        playerNetServerHandler.sendPacket(new SSetSlotPacket(-1, -1, player.inventory.getItemStack()));
    }

    /**
     * @return Container selection options for inventory tweaks.
     */
    @ContainerSectionCallback
    public Map<ContainerSection, List<Slot>> getContainerSelection() {
        try {
            Map<ContainerSection, List<Slot>> selection = Maps.newHashMap();
            List<Slot> chest = Lists.newArrayList();
            List<Slot> playerInventory = Lists.newArrayList();
            for (int i = 0; i < getSizeInventory(); i++) {
                chest.add(this.getSlot(i));
            }

            for (int i = getSizeInventory(); i < getSizeInventory() + player.inventory.mainInventory.size(); i++) {
                playerInventory.add(this.getSlot(i));
            }
            selection.put(ContainerSection.CHEST, chest);
            selection.put(ContainerSection.INVENTORY, playerInventory);
            return selection;
        } catch (RuntimeException e) {
            System.out.println("Size inv " + getSizeInventory());
            System.out.println("Player size inv " + player.inventory.mainInventory.size());
            System.out.println("Available slots " + inventorySlots.size());
            throw e;
        }
    }
}