package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.cyclops.colossalchests2.network.ChestNetwork;
import org.cyclops.colossalchests2.network.packet.ClientboundChestSlotsPacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * The chest GUI. Chest slots are not vanilla slots, as their counts do not fit in item stacks: the screen draws
 * them, clicks reach the server as {@link ChestClickAction}s, and the server sends changed slots to viewers.
 * The player inventory and the cursor are vanilla.
 * @author rubensworks
 */
public class ContainerChest extends AbstractContainerMenu {

    /**
     * How far from the structure a player can be while using it.
     */
    public static final double MAX_DISTANCE = 8;
    public static final int MAX_QUERY_LENGTH = 64;

    private final Player player;
    private final BlockPos corePos;
    @Nullable
    private final BlockEntityChestCore core;
    private final ChestLayout layout;

    // On the client what the server sent, on the server what it last sent.
    private final DeepSlot[] chestSlots;
    private final long[] capacities;
    private int[] view;
    private long depth;
    private ChestSettings settings = ChestSettings.DEFAULT;

    // Server only.
    private String query = "";
    private final BitSet dirtySlots = new BitSet();
    private boolean stateDirty = true;
    @Nullable
    private ChestSettings sentSettings;

    /**
     * Client-side constructor.
     */
    public ContainerChest(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos(), data.readVarInt(), null);
    }

    /**
     * Server-side constructor.
     */
    public ContainerChest(int id, Inventory inventory, BlockEntityChestCore core) {
        this(id, inventory, core.getBlockPos(), core.getStorage().getSlotCount(), core);
    }

    private ContainerChest(int id, Inventory inventory, BlockPos corePos, int slotCount, @Nullable BlockEntityChestCore core) {
        super(RegistryEntries.MENU_CHEST.value(), id);
        this.player = inventory.player;
        this.corePos = corePos;
        this.core = core;
        this.layout = ChestLayout.of(slotCount);
        this.chestSlots = new DeepSlot[slotCount];
        Arrays.fill(this.chestSlots, DeepSlot.EMPTY);
        this.capacities = new long[slotCount];
        this.view = IntStream.range(0, slotCount).toArray();
        addPlayerSlots(inventory);
        if (core != null && player instanceof ServerPlayer serverPlayer) {
            this.settings = core.getSettings();
            this.dirtySlots.set(0, slotCount);
            core.addViewer(serverPlayer);
        }
    }

    /**
     * Write what the client-side constructor reads.
     */
    public static void writeOpenData(FriendlyByteBuf data, BlockEntityChestCore core) {
        data.writeBlockPos(core.getBlockPos());
        data.writeVarInt(core.getStorage().getSlotCount());
    }

    private void addPlayerSlots(Inventory inventory) {
        int x = layout.getPlayerInventoryX();
        int y = layout.getPlayerInventoryY();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * 18, y + 58));
        }
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public ChestLayout getLayout() {
        return layout;
    }

    public int getChestSlotCount() {
        return chestSlots.length;
    }

    /**
     * @param slot A chest slot.
     * @return Its contents, as last synced.
     */
    public DeepSlot getChestSlot(int slot) {
        return chestSlots[slot];
    }

    public long getChestSlotCapacity(int slot) {
        return capacities[slot];
    }

    /**
     * @return If a slot holds more than it can, so it only allows extraction.
     */
    public boolean isChestSlotOverCapacity(int slot) {
        return chestSlots[slot].getCount() > capacities[slot];
    }

    /**
     * @return Chest slot indexes in display order.
     */
    public int[] getView() {
        return view;
    }

    /**
     * @return Stacks per slot.
     */
    public long getDepth() {
        return depth;
    }

    public ChestSettings getSettings() {
        return settings;
    }

    public boolean isFor(BlockEntityChestCore core) {
        return this.core == core;
    }

    // Server

    /**
     * Called by the core when slots changed.
     */
    public void markSlotsDirty(int[] changed) {
        for (int slot : changed) {
            if (slot < chestSlots.length) {
                dirtySlots.set(slot);
            }
        }
    }

    /**
     * Called by the core when its settings changed.
     */
    public void onSettingsChanged(ChestSettings settings) {
        this.settings = settings;
        this.stateDirty = true;
    }

    /**
     * Apply a click on a chest slot.
     */
    public void handleChestClick(ServerPlayer player, int slot, ChestClickAction action) {
        if (core == null || !core.isFormed() || slot < 0 || slot >= Math.min(chestSlots.length, core.getStorage().getSlotCount())) {
            return;
        }
        setCarried(ChestClickLogic.click(core.getStorage(), slot, action, getCarried(), stack -> {
            player.getInventory().add(stack);
            return stack;
        }));
    }

    /**
     * Apply a new search query and settings from the GUI.
     */
    public void handleSettings(String query, ChestSettings settings) {
        if (core == null) {
            return;
        }
        this.query = query.length() > MAX_QUERY_LENGTH ? query.substring(0, MAX_QUERY_LENGTH) : query;
        this.stateDirty = true;
        if (!settings.equals(core.getSettings())) {
            core.setSettings(settings);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (core != null && player instanceof ServerPlayer serverPlayer) {
            sendChestChanges(serverPlayer);
        }
    }

    private void sendChestChanges(ServerPlayer serverPlayer) {
        ChestStorage storage = core.getStorage();
        if (!dirtySlots.isEmpty()) {
            int[] changed = dirtySlots.stream().filter(slot -> slot < storage.getSlotCount()).toArray();
            dirtySlots.clear();
            DeepSlot[] contents = new DeepSlot[changed.length];
            long[] changedCapacities = new long[changed.length];
            for (int i = 0; i < changed.length; i++) {
                int slot = changed[i];
                chestSlots[slot] = storage.getSlot(slot);
                capacities[slot] = storage.getCapacity(slot);
                contents[i] = chestSlots[slot];
                changedCapacities[i] = capacities[slot];
            }
            ChestNetwork.sendToPlayer(
                    new ClientboundChestSlotsPacket(containerId, changed, contents, changedCapacities), serverPlayer);
            // Contents decide the order while searching or sorting.
            stateDirty |= !query.isEmpty() || settings.sortMode() != ChestSortMode.NONE;
        }
        long newDepth = storage.getProfile().depth();
        if (stateDirty || newDepth != depth) {
            stateDirty = false;
            int[] newView = ChestView.compute(storage, query, settings.sortMode(), stack -> stack.getHoverName().getString());
            newView = Arrays.stream(newView).filter(slot -> slot < chestSlots.length).toArray();
            if (!Arrays.equals(newView, view) || newDepth != depth || !Objects.equals(sentSettings, settings)) {
                view = newView;
                depth = newDepth;
                sentSettings = settings;
                ChestNetwork.sendToPlayer(
                        new ClientboundChestStatePacket(containerId, depth, settings, view), serverPlayer);
            }
        }
    }

    // Client

    public void applySlots(int[] changed, DeepSlot[] contents, long[] changedCapacities) {
        for (int i = 0; i < changed.length; i++) {
            if (changed[i] >= 0 && changed[i] < chestSlots.length) {
                chestSlots[changed[i]] = contents[i];
                capacities[changed[i]] = changedCapacities[i];
            }
        }
    }

    public void applyState(long depth, ChestSettings settings, int[] view) {
        this.depth = depth;
        this.settings = settings;
        this.view = Arrays.stream(view).filter(slot -> slot >= 0 && slot < chestSlots.length).toArray();
    }

    // Vanilla

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Only player slots exist, shift-clicking one moves it into the chest.
        if (core == null || index < 0 || index >= slots.size() || !core.isFormed()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        long inserted = core.getStorage().insert(stack, stack.getCount(), false);
        if (inserted > 0) {
            stack.shrink((int) inserted);
            slot.setChanged();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (core == null) {
            return true;
        }
        ChestStructure structure = core.getStructure();
        if (core.isRemoved() || structure == null) {
            return false;
        }
        AABB bounds = new AABB(structure.min().getX(), structure.min().getY(), structure.min().getZ(),
                structure.max().getX() + 1, structure.max().getY() + 1, structure.max().getZ() + 1).inflate(MAX_DISTANCE);
        return bounds.contains(player.position());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (core != null && player instanceof ServerPlayer serverPlayer) {
            core.removeViewer(serverPlayer);
        }
    }

}
