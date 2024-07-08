package org.cyclops.colossalchests.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetContentPacketWindow;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetSlotPacketLarge;
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

    public ContainerColossalChest(int id, Inventory playerInventory, FriendlyByteBuf data) {
        this(id, playerInventory, new LargeInventory(data.readInt(), 64));
    }

    public ContainerColossalChest(int id, Inventory playerInventory, Container inventory) {
        super(RegistryEntries.CONTAINER_COLOSSAL_CHEST.get(), id, playerInventory, inventory, Collections.<Slot>emptyList(), (item, pattern) -> true);

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

    protected Slot makeSlot(Container inventory, int index, int row, int column) {
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
    public void addSlotListener(ContainerListener listener) {
        if (this.containerListeners.contains(listener)) {
            throw new IllegalArgumentException("Listener already listening");
        } else {
            this.containerListeners.add(listener);
            if(listener instanceof ServerPlayer) {
                updateCraftingInventory((ServerPlayer) listener, getItems());
            } else {
                // TODO: rm?
                //listener.refreshContainer(this, this.getItems());
            }
            this.broadcastChanges();
        }
    }

    @Override
    public void broadcastChanges() {
        int newState = ((SimpleInventory) inventory).getState();
        if (lastInventoryState != newState) {
            lastInventoryState = newState;
            detectAndSendChangesOverride();
        }
    }

    // Custom implementation of Container#detectAndSendChanges
    protected void detectAndSendChangesOverride() {
        for (int i = 0; i < this.getSizeInventory(); ++i) {
            ItemStack itemstack = this.slots.get(i).getItem();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.matches(itemstack1, itemstack)) {
                itemstack1 = itemstack.isEmpty() ? ItemStack.EMPTY : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                if (!firstDetectionCheck) {
                    for (int j = 0; j < this.containerListeners.size(); ++j) {
                        ContainerListener listener = this.containerListeners.get(j);
                        if (listener instanceof ServerPlayer) {
                            sendSlotContentsToPlayer((ServerPlayer) listener, this, i, itemstack1);
                        } else {
                            listener.slotChanged(this, i, itemstack1);
                        }
                    }
                }
            }
        }
        firstDetectionCheck = false;
    }

    // Adapted from EntityPlayerMP#sendSlotContents
    protected void sendSlotContentsToPlayer(ServerPlayer player, AbstractContainerMenu containerToSend, int slotInd, ItemStack stack) {
        if (!(containerToSend.getSlot(slotInd) instanceof ResultSlot)) {
            ColossalChests._instance.getPacketHandler().sendToPlayer(
                    new ClientboundContainerSetSlotPacketLarge(containerToSend.containerId, getStateId(), slotInd, stack), player);
        }
    }

    protected int getTagSize(Tag tag) {
        if (tag instanceof NumericTag || tag instanceof EndTag) {
            return 1;
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag) tag;
            int size = 0;
            for (String key : compound.getAllKeys()) {
                size += getTagSize(compound.get(key));
            }
            return size;
        }
        if (tag instanceof ByteArrayTag) {
            return ((ByteArrayTag) tag).getAsByteArray().length;
        }
        if (tag instanceof IntArrayTag) {
            return ((IntArrayTag) tag).getAsIntArray().length * 32;
        }
        if (tag instanceof ListTag) {
            ListTag list = (ListTag) tag;
            int size = 0;
            for (int i = 0; i < list.size(); i++) {
                size += getTagSize(list.get(i));
            }
            return size;
        }
        if (tag instanceof StringTag) {
            try {
                return ((StringTag) tag).getAsString().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {}
        }
        return tag.toString().length();
    }

    // Modified from ServerPlayer#updateCraftingInventory
    public void updateCraftingInventory(ServerPlayer player, List<ItemStack> allItems) {
        int maxBufferSize = GeneralConfig.maxPacketBufferSize;
        // Custom packet sending to be able to handle large inventories
        ServerGamePacketListenerImpl playerNetServerHandler = player.connection;
        // Modification of logic in EntityPlayerMP#updateCraftingInventory
        CompoundTag sendBuffer = new CompoundTag();
        ListTag sendList = new ListTag();
        sendBuffer.put("stacks", sendList);
        int i = 0;
        int bufferSize = 0;
        int sent = 0;
        for (ItemStack itemStack : allItems) {
            if (itemStack != null) {
                CompoundTag tag = new CompoundTag();
                tag.putInt("slot", i);
                tag.put("stack", ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, itemStack)
                        .getOrThrow(JsonParseException::new));
                int tagSize = getTagSize(tag);
                if (bufferSize + tagSize + 100 < maxBufferSize) {
                    sendList.add(tag);
                    bufferSize += tagSize;
                } else {
                    // Flush
                    ColossalChests._instance.getPacketHandler().sendToPlayer(new ClientboundContainerSetContentPacketWindow(containerId, getStateId(), sendBuffer), player);
                    sendBuffer = new CompoundTag();
                    sendList = new ListTag();
                    sendList.add(tag);
                    sendBuffer.put("stacks", sendList);
                    bufferSize = tagSize;
                }
            }
            i++;
        }
        if (sendList.size() > 0) {
            // Flush
            ColossalChests._instance.getPacketHandler().sendToPlayer(new ClientboundContainerSetContentPacketWindow(containerId, getStateId(), sendBuffer), player);
        }
        playerNetServerHandler.send(new ClientboundContainerSetSlotPacket(-1, getStateId(), -1, getCarried()));
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

            for (int i = getSizeInventory(); i < getSizeInventory() + player.getInventory().items.size(); i++) {
                playerInventory.add(this.getSlot(i));
            }
            selection.put(ContainerSection.CHEST, chest);
            selection.put(ContainerSection.INVENTORY, playerInventory);
            return selection;
        } catch (RuntimeException e) {
            System.out.println("Size inv " + getSizeInventory());
            System.out.println("Player size inv " + player.getInventory().items.size());
            System.out.println("Available slots " + slots.size());
            throw e;
        }
    }
}
