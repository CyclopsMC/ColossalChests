package org.cyclops.colossalchests2.inventory;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.cyclops.colossalchests2.network.ChestNetwork;
import org.cyclops.colossalchests2.network.packet.ClientboundChestSlotsPacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.CompressionFamiliesCache;
import org.cyclops.colossalchests2.storage.CompressionFamily;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgradeRules;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgrade;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;

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
    public static final int MAX_DRAG_SLOTS = GeneralConfig.HARD_MAX_SLOTS;
    public static final int UPGRADE_SLOT_X = -19;
    public static final int UPGRADE_SLOT_Y = 8;

    private final Player player;
    private final BlockPos corePos;
    @Nullable
    private final BlockEntityChestCore core;
    private final ChestLayout layout;
    private final Container upgradeContainer;
    private final int[] maxUpgradeCounts;
    // Per upgrade, 1 if a chest of another material takes more of it.
    private final int[] betterMaterialTakesMore;
    private final int upgradeSlotsStart;

    // On the client what the server sent, on the server what it last sent.
    private final DeepSlot[] chestSlots;
    private final long[] capacities;
    private CapacityProfile profile = CapacityProfile.ofDepth(0);
    // Per upgrade slot, the chest slots that keep its upgrade from being removed.
    private int[] upgradeRemovalProblems;

    // Server only.
    private final BitSet dirtySlots = new BitSet();
    private boolean upgradesDirty = true;
    @Nullable
    private int[] sentUpgradeRemovalProblems;

    /**
     * Client-side constructor.
     */
    public ContainerChest(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos(), data.readVarInt(), new SimpleContainer(data.readVarInt()), readUpgradeArray(data),
                readUpgradeArray(data), null);
    }

    /**
     * Server-side constructor.
     */
    public ContainerChest(int id, Inventory inventory, BlockEntityChestCore core) {
        this(id, inventory, core.getBlockPos(), core.getStorage().getSlotCount(), core.getUpgrades(), getMaxUpgradeCounts(core),
                getBetterMaterialTakesMore(core), core);
    }

    private ContainerChest(int id, Inventory inventory, BlockPos corePos, int slotCount, Container upgradeContainer,
                           int[] maxUpgradeCounts, int[] betterMaterialTakesMore, @Nullable BlockEntityChestCore core) {
        super(RegistryEntries.MENU_CHEST.value(), id);
        this.player = inventory.player;
        this.corePos = corePos;
        this.core = core;
        this.layout = ChestLayout.of(slotCount);
        this.chestSlots = new DeepSlot[slotCount];
        Arrays.fill(this.chestSlots, DeepSlot.EMPTY);
        this.capacities = new long[slotCount];
        this.upgradeContainer = upgradeContainer;
        this.maxUpgradeCounts = maxUpgradeCounts;
        this.betterMaterialTakesMore = betterMaterialTakesMore;
        this.upgradeRemovalProblems = new int[upgradeContainer.getContainerSize()];
        addPlayerSlots(inventory);
        this.upgradeSlotsStart = slots.size();
        for (int slot = 0; slot < upgradeContainer.getContainerSize(); slot++) {
            addSlot(new UpgradeSlot(upgradeContainer, slot, UPGRADE_SLOT_X, UPGRADE_SLOT_Y + slot * 18));
        }
        if (core != null && player instanceof ServerPlayer serverPlayer) {
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
        data.writeVarInt(core.getUpgrades().getContainerSize());
        data.writeVarIntArray(getMaxUpgradeCounts(core));
        data.writeVarIntArray(getBetterMaterialTakesMore(core));
    }

    private static int[] getMaxUpgradeCounts(BlockEntityChestCore core) {
        return ChestUpgrades.VALUES.stream().mapToInt(core::getMaxUpgradeCount).toArray();
    }

    private static int[] getBetterMaterialTakesMore(BlockEntityChestCore core) {
        return ChestUpgrades.VALUES.stream().mapToInt(upgrade -> ChestMaterial.VALUES.stream()
                .anyMatch(material -> ChestUpgradeRules.getMaxCount(upgrade, material.id()) > core.getMaxUpgradeCount(upgrade)) ? 1 : 0).toArray();
    }

    private static int[] readUpgradeArray(FriendlyByteBuf data) {
        int[] values = data.readVarIntArray();
        return values.length == ChestUpgrades.VALUES.size() ? values : new int[ChestUpgrades.VALUES.size()];
    }

    /**
     * An upgrade slot. It takes an upgrade if the chest takes one more of it, and gives it back if contents
     * would still fit without it.
     */
    public class UpgradeSlot extends Slot {

        public UpgradeSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (core != null) {
                return container.canPlaceItem(getContainerSlot(), stack);
            }
            ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(stack);
            return upgrade != null && !hasItem() && getUpgradeSet().count(upgrade) < getMaxUpgradeCount(upgrade);
        }

        @Override
        public boolean mayPickup(Player player) {
            if (core != null) {
                return core.getUpgradeRemovalProblems(getContainerSlot()).isOk();
            }
            return getUpgradeRemovalProblems(getContainerSlot()) == 0;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

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
     * @param slot A chest slot.
     * @param type An item type.
     * @return If the type may go into the slot as last synced, ignoring how full it is.
     */
    public boolean canChestSlotAccept(int slot, ItemStack type) {
        DeepSlot deepSlot = chestSlots[slot];
        return !type.isEmpty() && (deepSlot.isEmpty() || deepSlot.matches(getFamily(type).map(family -> new ItemStack(family.largest().item())).orElse(type)));
    }

    /**
     * @param slot A chest slot.
     * @param type A type the slot can hold, for a compressed slot any form of its family.
     * @return How many whole items of the type the slot holds, as last synced.
     */
    public long getChestSlotAmount(int slot, ItemStack type) {
        DeepSlot deepSlot = chestSlots[slot];
        Optional<CompressionFamily> family = getFamily(type);
        if (family.isPresent() && !deepSlot.isEmpty()) {
            CompressionFamily f = family.get();
            return f.fromBaseUnits(f.indexOf(type), f.toBaseUnits(0, deepSlot.getCount()) + deepSlot.getRemainder());
        }
        return deepSlot.matches(type) ? deepSlot.getCount() : 0;
    }

    /**
     * @param type An item type.
     * @return Its compression family, while the chest compresses.
     */
    private Optional<CompressionFamily> getFamily(ItemStack type) {
        return getUpgradeSet().has(ChestUpgrades.COMPRESSION) ? CompressionFamiliesCache.get(player.level()).find(type) : Optional.empty();
    }

    /**
     * @param slot A chest slot.
     * @param type An item type.
     * @return How much of the type fits in the slot as last synced.
     */
    public long getChestSlotSpace(int slot, ItemStack type) {
        if (!canChestSlotAccept(slot, type)) {
            return 0;
        }
        DeepSlot deepSlot = chestSlots[slot];
        Optional<CompressionFamily> family = getFamily(type);
        if (family.isPresent()) {
            // Counted in base units, like the storage does.
            CompressionFamily f = family.get();
            long capacity = deepSlot.isEmpty() ? profile.capacityFor(new ItemStack(f.largest().item()).getMaxStackSize()) : capacities[slot];
            long free = f.toBaseUnits(0, capacity) - f.toBaseUnits(0, deepSlot.getCount()) - deepSlot.getRemainder();
            return Math.max(0, free) / f.get(f.indexOf(type)).baseUnits();
        }
        long capacity = deepSlot.isEmpty() ? profile.capacityFor(type.getMaxStackSize()) : capacities[slot];
        return Math.max(0, capacity - deepSlot.getCount());
    }

    /**
     * @return The capacity rules of the slots.
     */
    public CapacityProfile getProfile() {
        return profile;
    }

    public int getUpgradeSlotsStart() {
        return upgradeSlotsStart;
    }

    public int getUpgradeSlotCount() {
        return upgradeContainer.getContainerSize();
    }

    /**
     * @return The installed upgrades, as last synced on the client.
     */
    public UpgradeSet getUpgradeSet() {
        List<ItemStack> stacks = Lists.newArrayList();
        for (int slot = 0; slot < upgradeContainer.getContainerSize(); slot++) {
            stacks.add(upgradeContainer.getItem(slot));
        }
        return UpgradeSet.of(stacks);
    }

    /**
     * @param upgrade An upgrade.
     * @return How many of the upgrade this chest takes.
     */
    public int getMaxUpgradeCount(ChestUpgrade upgrade) {
        int index = ChestUpgrades.VALUES.indexOf(upgrade);
        return index >= 0 ? maxUpgradeCounts[index] : 0;
    }

    /**
     * @param stack A stack to insert into an upgrade slot.
     * @return Why the chest does not take it, or null if it does.
     */
    @Nullable
    public Component getUpgradeInsertProblem(ItemStack stack) {
        ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(stack);
        if (upgrade == null) {
            return Component.translatable("gui.colossalchests2.upgrade.not_an_upgrade");
        }
        int max = getMaxUpgradeCount(upgrade);
        if (max == 0) {
            return Component.translatable("gui.colossalchests2.upgrade.none_allowed", stack.getHoverName());
        }
        if (getUpgradeSet().count(upgrade) >= max) {
            return Component.translatable("gui.colossalchests2.upgrade.max_reached", max, stack.getHoverName());
        }
        return null;
    }

    /**
     * @param stack A stack refused by the upgrade slots.
     * @return If a chest of another material takes more of it.
     */
    public boolean doesBetterMaterialTakeMore(ItemStack stack) {
        ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(stack);
        return upgrade != null && betterMaterialTakesMore[ChestUpgrades.VALUES.indexOf(upgrade)] == 1;
    }

    /**
     * @return If an upgrade slot is free.
     */
    public boolean hasFreeUpgradeSlot() {
        for (int slot = 0; slot < upgradeContainer.getContainerSize(); slot++) {
            if (upgradeContainer.getItem(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param upgradeSlot An upgrade slot index.
     * @return The number of chest slots that keep its upgrade from being removed.
     */
    public int getUpgradeRemovalProblems(int upgradeSlot) {
        return upgradeRemovalProblems[upgradeSlot];
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
     * Called by the core when its upgrades changed.
     */
    public void onUpgradesChanged() {
        this.upgradesDirty = true;
    }

    /**
     * Apply a click on a chest slot.
     */
    public void handleChestClick(ServerPlayer player, int slot, ChestClickAction action) {
        if (core == null || !core.isFormed()) {
            return;
        }
        if (action.isMarkAction()) {
            handleMark(slot, action);
            return;
        }
        if (slot < 0 || slot >= Math.min(chestSlots.length, core.getStorage().getSlotCount())) {
            return;
        }
        setCarried(ChestClickLogic.click(core.getStorage(), slot, action, getCarried(), stack -> {
            player.getInventory().add(stack);
            return stack;
        }));
    }

    private void handleMark(int slot, ChestClickAction action) {
        ChestStorage storage = core.getStorage();
        boolean voidAction = action == ChestClickAction.TOGGLE_VOID || action == ChestClickAction.VOID_ALL || action == ChestClickAction.CLEAR_VOIDS;
        if (!core.getUpgradeSet().has(voidAction ? ChestUpgrades.VOID : ChestUpgrades.LOCK)) {
            return;
        }
        boolean validSlot = slot >= 0 && slot < Math.min(chestSlots.length, storage.getSlotCount());
        switch (action) {
            case TOGGLE_VOID -> {
                if (validSlot) {
                    storage.setVoiding(slot, !storage.getSlot(slot).isVoiding());
                }
            }
            case VOID_ALL -> storage.voidAllFilled();
            case CLEAR_VOIDS -> storage.clearVoids();
            case TOGGLE_LOCK -> {
                if (validSlot) {
                    storage.setLocked(slot, !storage.getSlot(slot).isLocked());
                }
            }
            case LOCK_TO_CURSOR -> {
                if (validSlot && storage.getSlot(slot).getCount() == 0) {
                    storage.lockTo(slot, getCarried());
                }
            }
            case LOCK_ALL -> storage.lockAllFilled();
            case CLEAR_LOCKS -> storage.clearLocks();
            default -> {
            }
        }
    }

    /**
     * Pick the form to take out of a compressed slot.
     */
    public void handleForm(int slot, ItemStack form) {
        if (core == null || !core.isFormed() || slot < 0 || slot >= Math.min(chestSlots.length, core.getStorage().getSlotCount())) {
            return;
        }
        ChestStorage storage = core.getStorage();
        boolean ofFamily = storage.getFamily(storage.getSlot(slot).getPrototype()).map(family -> family.indexOf(form) >= 0).orElse(false);
        if (ofFamily) {
            storage.setCompressionForm(slot, form.getItem());
        }
    }

    /**
     * Spread the cursor stack over chest slots, like dragging over vanilla slots.
     */
    public void handleChestDrag(int[] dragged, boolean oneEach) {
        if (core == null || !core.isFormed()) {
            return;
        }
        int[] valid = Arrays.stream(dragged).filter(slot -> slot >= 0 && slot < Math.min(chestSlots.length, core.getStorage().getSlotCount()))
                .distinct().toArray();
        setCarried(ChestClickLogic.drag(core.getStorage(), valid, oneEach, getCarried()));
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
        if (upgradesDirty || !dirtySlots.isEmpty()) {
            upgradesDirty = false;
            for (int slot = 0; slot < upgradeRemovalProblems.length; slot++) {
                upgradeRemovalProblems[slot] = core.getUpgradeRemovalProblems(slot).offendingSlots().size();
            }
        }
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
        }
        CapacityProfile newProfile = storage.getProfile();
        // Nothing was sent yet at first, so the first check always sends.
        if (!newProfile.equals(profile) || !Arrays.equals(sentUpgradeRemovalProblems, upgradeRemovalProblems)) {
            profile = newProfile;
            sentUpgradeRemovalProblems = upgradeRemovalProblems.clone();
            ChestNetwork.sendToPlayer(new ClientboundChestStatePacket(containerId, profile, sentUpgradeRemovalProblems), serverPlayer);
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

    public void applyState(CapacityProfile profile, int[] upgradeRemovalProblems) {
        this.profile = profile;
        if (upgradeRemovalProblems.length == this.upgradeRemovalProblems.length) {
            this.upgradeRemovalProblems = upgradeRemovalProblems;
        }
    }

    // Vanilla

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (core == null || index < 0 || index >= slots.size() || !core.isFormed()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        if (index >= upgradeSlotsStart) {
            // Out of an upgrade slot, into the player inventory.
            if (slot.mayPickup(player) && moveItemStackTo(stack, 0, upgradeSlotsStart, true)) {
                slot.setChanged();
            }
            return ItemStack.EMPTY;
        }
        if (ItemChestUpgrade.getUpgrade(stack) != null && moveItemStackTo(stack, upgradeSlotsStart, slots.size(), false)) {
            slot.setChanged();
            return ItemStack.EMPTY;
        }
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
