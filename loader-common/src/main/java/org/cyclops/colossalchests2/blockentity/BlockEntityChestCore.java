package org.cyclops.colossalchests2.blockentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWall;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.block.ChestSounds;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.capability.ItemHandlerLogic;
import org.cyclops.colossalchests2.capability.StorageSignals;
import org.cyclops.colossalchests2.config.MaterialProperties;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.multiblock.ChestShape;
import org.cyclops.colossalchests2.multiblock.ChestStructure;
import org.cyclops.colossalchests2.multiblock.LevelStructureView;
import org.cyclops.colossalchests2.multiblock.StructureDetector;
import org.cyclops.colossalchests2.network.ChestNetwork;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.CompressionFamilies;
import org.cyclops.colossalchests2.storage.CompressionFamiliesCache;
import org.cyclops.colossalchests2.storage.ResizeResult;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgradeInventory;
import org.cyclops.colossalchests2.upgrade.ChestUpgradeRules;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.colossalchests2.upgrade.ItemChestUpgrade;
import org.cyclops.colossalchests2.upgrade.UpgradeSet;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Block entity of a chest core: owns the storage and the cached structure.
 * While the structure is broken the core is dormant: contents stay, but its item storage is not exposed.
 * @author rubensworks
 */
public class BlockEntityChestCore extends BlockEntity implements MenuProvider, ChestUpgradeInventory.Owner {

    public static final int DATA_VERSION = 1;
    /**
     * How far from a chest players are credited with forming it.
     */
    public static final int FORMED_TRIGGER_RANGE = 16;
    /**
     * Block event that carries the number of viewers, which opens the lid on clients.
     */
    public static final int EVENT_VIEWERS = 1;
    private static final int UNLOADED_RETRY_TICKS = 20;

    public static CapabilityInvalidator capabilityInvalidator = CapabilityInvalidator.NOOP;

    private final ChestStorage storage;
    private final ItemHandlerLogic itemHandlerLogic;
    private final Set<ServerPlayer> viewers = Sets.newHashSet();
    private final ChestLid lid = new ChestLid();

    @Nullable
    private ChestStructure structure;
    private int lastSize;
    private List<BlockPos> decoratedPositions = List.of();
    private boolean registered;
    private boolean validationRequested = true;
    private int validationCooldown;
    private boolean contentsChanged;
    private final ChestUpgradeInventory upgrades;
    private boolean loadingUpgrades;
    private boolean reopenMenus;

    public BlockEntityChestCore(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.upgrades = new ChestUpgradeInventory(this, getMaterialProperties(state).upgradeSlots());
        this.storage = new ChestStorage(GeneralConfig.getBaseSlots(), ChestUpgradeRules.createProfile(0, UpgradeSet.EMPTY));
        this.storage.addListener(slot -> this.contentsChanged = true);
        this.itemHandlerLogic = new ItemHandlerLogic(this.storage);
    }

    public BlockEntityChestCore(BlockPos pos, BlockState state) {
        this(RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityChestCore core) {
        core.tick();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityChestCore core) {
        ChestStructure structure = core.getStructure();
        if (structure != null) {
            core.lid.setSpeed(ChestSounds.getLidSpeed(structure.size()));
        }
        core.lid.tick();
    }

    public ChestStorage getStorage() {
        return storage;
    }

    public ChestUpgradeInventory getUpgrades() {
        return upgrades;
    }

    public UpgradeSet getUpgradeSet() {
        return upgrades.getUpgradeSet();
    }

    /**
     * @return The material id of this core, or wood if the block is not a core.
     */
    public ResourceLocation getMaterialId() {
        return getMaterial(getBlockState()).id();
    }

    private static ChestMaterial getMaterial(BlockState state) {
        return state.getBlock() instanceof BlockChestCore block ? block.getMaterial() : ChestMaterial.WOOD;
    }

    private static MaterialProperties getMaterialProperties(BlockState state) {
        return getMaterial(state).getProperties();
    }

    /**
     * @param upgrade An upgrade.
     * @return How many of the upgrade this chest takes.
     */
    public int getMaxUpgradeCount(ChestUpgrade upgrade) {
        return ChestUpgradeRules.getMaxCount(upgrade, getMaterialId());
    }

    @Override
    public boolean canAddUpgrade(int slot, ChestUpgrade upgrade) {
        return slot < getMaterialProperties(getBlockState()).upgradeSlots() && ChestUpgradeRules.canAdd(getUpgradeSet(), upgrade, getMaterialId());
    }

    /**
     * @param slot An upgrade slot.
     * @return The chest slots that keep the upgrade in that slot from being removed, empty if it can be removed.
     */
    public ResizeResult getUpgradeRemovalProblems(int slot) {
        ChestUpgrade upgrade = ItemChestUpgrade.getUpgrade(upgrades.getItem(slot));
        return upgrade == null ? ResizeResult.OK : ChestUpgradeRules.getRemovalProblems(storage, lastSize, getUpgradeSet(), upgrade);
    }

    @Override
    public void onUpgradesChanged() {
        if (loadingUpgrades) {
            return;
        }
        int oldSlotCount = storage.getSlotCount();
        if (!getUpgradeSet().has(ChestUpgrades.LOCK)) {
            storage.clearLocks();
        }
        if (!getUpgradeSet().has(ChestUpgrades.VOID)) {
            storage.clearVoids();
        }
        updateCompression();
        applyProfile(false);
        setChanged();
        if (storage.getSlotCount() != oldSlotCount) {
            reopenMenus = true;
        }
        for (ServerPlayer viewer : viewers) {
            if (viewer.containerMenu instanceof ContainerChest menu && menu.isFor(this)) {
                menu.onUpgradesChanged();
            }
        }
    }

    public ItemHandlerLogic getItemHandlerLogic() {
        return itemHandlerLogic;
    }

    /**
     * @return The structure this core forms, or null while dormant.
     */
    @Nullable
    public ChestStructure getStructure() {
        return structure;
    }

    /**
     * @return The size of the last formed structure, or 0 if it never formed.
     */
    public int getLastSize() {
        return lastSize;
    }

    /**
     * @return If the structure is formed, so contents can be accessed.
     */
    public boolean isFormed() {
        return structure != null;
    }

    /**
     * @return The side the chest's lock faces, or south while dormant.
     */
    public Direction getFacing() {
        return structure == null ? Direction.SOUTH : ChestShape.getFacing(structure, worldPosition);
    }

    /**
     * Members of the formed structure that are not plain walls, such as the core itself.
     * These are the only positions that can draw something on top of the giant chest. Synced to clients.
     * @return Absolute positions, empty while dormant.
     */
    public List<BlockPos> getDecoratedPositions() {
        return decoratedPositions;
    }

    /**
     * @return The area this core renders in, the whole giant chest while formed.
     */
    public AABB getRenderBounds() {
        return structure == null ? new AABB(worldPosition) : ChestShape.getRenderBounds(structure);
    }

    /**
     * @param partialTick The partial tick.
     * @return How far the lid is open, from 0 to 1. Only meaningful on the client.
     */
    public float getOpenness(float partialTick) {
        return lid.getOpenness(partialTick);
    }

    public int getComparatorSignal() {
        return isFormed() ? StorageSignals.getComparatorSignal(storage) : 0;
    }

    /**
     * Open the chest GUI, which only works while formed.
     */
    public void openMenu(ServerPlayer player) {
        if (isFormed()) {
            IModHelpers.get().getMinecraftHelpers().openMenu(player, this, data -> ContainerChest.writeOpenData(data, this));
        }
    }

    /**
     * Tell a player opening the chest that some slots hold more than they can, so they only allow extraction.
     * This happens after re-forming smaller or lowering config values.
     */
    public void warnIfOverCapacity(ServerPlayer player) {
        int overCapacity = 0;
        for (int slot = 0; slot < storage.getSlotCount(); slot++) {
            if (storage.isExtractOnly(slot)) {
                overCapacity++;
            }
        }
        if (overCapacity > 0) {
            player.sendSystemMessage(Component.translatable("chest.colossalchests2.over_capacity", overCapacity));
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock() instanceof BlockChestCore block
                ? Component.translatable("container.colossalchests2.chest", block.getMaterial().getDisplayName())
                : Component.translatable("container.colossalchests2.chest", "");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return isFormed() ? new ContainerChest(containerId, inventory, this) : null;
    }

    /**
     * Validate the structure now instead of on the next tick.
     */
    public void validateNow() {
        if (!registered) {
            ChestCoreIndex.register(level, worldPosition);
            registered = true;
        }
        validationRequested = false;
        validate();
    }

    /**
     * Move all upgrades to the first slots, so they fit when the slot count shrinks.
     */
    public void compactUpgrades() {
        upgrades.load(upgrades.getItems().stream().filter(stack -> !stack.isEmpty()).toList());
    }

    /**
     * Revalidate the structure on the next tick. Cheap to call often, validation happens at most once per tick.
     */
    public void requestValidation() {
        this.validationRequested = true;
    }

    /**
     * Start sending slot changes to a player, used by the chest menu.
     * @param player The player.
     */
    public void addViewer(ServerPlayer player) {
        if (viewers.add(player)) {
            storage.markAllDirty();
            onViewersChanged(viewers.size() == 1 ? Boolean.TRUE : null);
        }
    }

    public void removeViewer(ServerPlayer player) {
        if (viewers.remove(player)) {
            onViewersChanged(viewers.isEmpty() ? Boolean.FALSE : null);
        }
    }

    /**
     * @param open If the chest was opened or closed, or null if neither.
     */
    private void onViewersChanged(@Nullable Boolean open) {
        level.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_VIEWERS, viewers.size());
        if (open != null && structure != null) {
            ChestSounds.play(level, getCenter(structure), structure.size(), open);
        }
    }

    private Vec3 getCenter(ChestStructure structure) {
        double half = structure.size() / 2D;
        return new Vec3(structure.min().getX() + half, structure.min().getY() + half, structure.min().getZ() + half);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_VIEWERS) {
            lid.shouldBeOpen(param > 0);
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public Set<ServerPlayer> getViewers() {
        return Collections.unmodifiableSet(viewers);
    }

    protected void tick() {
        if (!registered) {
            ChestCoreIndex.register(level, worldPosition);
            registered = true;
            validationRequested = true;
        }
        if (validationCooldown > 0) {
            validationCooldown--;
        } else if (validationRequested) {
            validationRequested = false;
            validate();
        }
        if (contentsChanged) {
            contentsChanged = false;
            setChanged();
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            for (BlockPos pos : decoratedPositions) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof BlockChestFunctionalWall wall && wall.getType() == WallType.REDSTONE
                        && level.getBlockEntity(pos) instanceof BlockEntityChestWall redstoneWall) {
                    redstoneWall.updateRedstoneSignal();
                }
            }
        }
        if (!viewers.isEmpty() && storage.hasDirtySlots()) {
            onDirtySlots(storage.drainDirtySlots());
        }
        if (reopenMenus) {
            reopenMenus = false;
            reopenMenus();
        }
    }

    /**
     * Reopen the GUI of all viewers, as the slot grid only changes size between openings.
     * The item on their cursor stays there. Players without a client, such as fake players, keep their menu.
     */
    protected void reopenMenus() {
        for (ServerPlayer viewer : List.copyOf(viewers)) {
            if (viewer.containerMenu instanceof ContainerChest menu && menu.isFor(this) && ChestNetwork.hasClient(viewer)) {
                ItemStack carried = menu.getCarried();
                menu.setCarried(ItemStack.EMPTY);
                openMenu(viewer);
                viewer.containerMenu.setCarried(carried);
                viewer.containerMenu.broadcastChanges();
            }
        }
    }

    /**
     * Called with the slots that changed since the last call, while players view the chest.
     * @param slots Changed slot indexes.
     */
    protected void onDirtySlots(int[] slots) {
        for (ServerPlayer viewer : viewers) {
            if (viewer.containerMenu instanceof ContainerChest menu && menu.isFor(this)) {
                menu.markSlotsDirty(slots);
            }
        }
    }

    protected void validate() {
        if (!(getBlockState().getBlock() instanceof BlockChestCore coreBlock)) {
            return;
        }
        int maxSize = coreBlock.getMaterial().getProperties().maxSize();
        StructureDetector.Result result = StructureDetector.detect(new LevelStructureView(level, worldPosition), worldPosition, maxSize);
        switch (result.state()) {
            case VALID -> setStructure(result.structure());
            case INVALID -> setStructure(null);
            case UNLOADED -> {
                validationRequested = true;
                validationCooldown = UNLOADED_RETRY_TICKS;
            }
        }
    }

    protected void setStructure(@Nullable ChestStructure newStructure) {
        boolean formedState = getBlockState().getValue(BlockChestCore.FORMED);
        List<BlockPos> newDecorated = findDecoratedPositions(newStructure);
        if (Objects.equals(structure, newStructure) && formedState == (newStructure != null)) {
            // A wall can be swapped without the chest dissolving, such as by commands.
            if (newStructure != null) {
                boolean wallsChanged = false;
                for (BlockPos pos : newStructure.shell()) {
                    wallsChanged |= setWallFormed(pos, true);
                }
                if (wallsChanged) {
                    invalidateCapabilities(newStructure);
                }
            }
            if (!newDecorated.equals(decoratedPositions)) {
                decoratedPositions = newDecorated;
                syncToClients();
            }
            return;
        }
        this.decoratedPositions = newDecorated;
        ChestStructure oldStructure = this.structure;
        this.structure = newStructure;
        if (oldStructure != null) {
            for (BlockPos pos : oldStructure.shell()) {
                if (newStructure == null || !newStructure.isOnShell(pos)) {
                    setWallFormed(pos, false);
                }
            }
        }
        if (newStructure != null) {
            for (BlockPos pos : newStructure.shell()) {
                setWallFormed(pos, true);
            }
            lastSize = newStructure.size();
            applyProfile(false);
            if (!newStructure.equals(oldStructure) && getBlockState().getBlock() instanceof BlockChestCore coreBlock) {
                onFormed(newStructure, coreBlock.getMaterial());
            }
        }
        level.setBlock(worldPosition, getBlockState().setValue(BlockChestCore.FORMED, newStructure != null), Block.UPDATE_CLIENTS);
        invalidateCapabilities(oldStructure);
        invalidateCapabilities(newStructure);
        contentsChanged = true;
        setChanged();
        syncToClients();
    }

    /**
     * Credit players near a chest that just formed, as it forms by itself once its last block is placed.
     */
    private void onFormed(ChestStructure structure, ChestMaterial material) {
        AABB area = new AABB(structure.min().getX(), structure.min().getY(), structure.min().getZ(),
                structure.max().getX() + 1, structure.max().getY() + 1, structure.max().getZ() + 1).inflate(FORMED_TRIGGER_RANGE);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            RegistryEntries.TRIGGER_CHEST_FORMED.value().trigger(player, material, structure.size());
        }
    }

    private List<BlockPos> findDecoratedPositions(@Nullable ChestStructure structure) {
        if (structure == null) {
            return List.of();
        }
        List<BlockPos> positions = Lists.newArrayList();
        for (BlockPos pos : structure.shell()) {
            if (!(level.getBlockState(pos).getBlock() instanceof BlockChestWall wall && wall.isPlain())) {
                positions.add(pos);
            }
        }
        return ImmutableList.copyOf(positions);
    }

    private void syncToClients() {
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    /**
     * The core block is being removed: release the walls without touching the core's own block.
     */
    public void dissolve() {
        ChestStructure oldStructure = this.structure;
        this.structure = null;
        this.decoratedPositions = List.of();
        if (oldStructure != null) {
            for (BlockPos pos : oldStructure.shell()) {
                setWallFormed(pos, false);
            }
            invalidateCapabilities(oldStructure);
        }
    }

    /**
     * @return If the wall changed.
     */
    private boolean setWallFormed(BlockPos pos, boolean formed) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BlockChestWall && state.getValue(BlockChestWall.FORMED) != formed) {
            level.setBlock(pos, state.setValue(BlockChestWall.FORMED, formed), Block.UPDATE_CLIENTS);
            return true;
        }
        return false;
    }

    private void invalidateCapabilities(@Nullable ChestStructure structure) {
        capabilityInvalidator.invalidate(level, worldPosition);
        if (structure != null) {
            for (BlockPos pos : structure.shell()) {
                capabilityInvalidator.invalidate(level, pos);
            }
        }
        onCapabilitiesChanged();
    }

    /**
     * Called when the exposed capabilities may have changed.
     */
    protected void onCapabilitiesChanged() {
    }


    /**
     * Apply the capacity for the last formed size and the current config.
     * Slots that no longer fit become extract-only, nothing is ever deleted.
     * @param warnIfOverCapacity If over-capacity slots should be logged, for config changes between sessions.
     */
    protected void applyProfile(boolean warnIfOverCapacity) {
        UpgradeSet upgradeSet = getUpgradeSet();
        ResizeResult result = storage.forceProfile(ChestUpgradeRules.createProfile(lastSize, upgradeSet));
        int highestFilled = -1;
        for (int slot = 0; slot < storage.getSlotCount(); slot++) {
            if (storage.getSlot(slot).getCount() > 0) {
                highestFilled = slot;
            }
        }
        storage.setSlotCount(Math.max(ChestUpgradeRules.getSlotCount(upgradeSet), highestFilled + 1));
        if (warnIfOverCapacity && lastSize > 0 && !result.isOk()) {
            ColossalChestsInstance.MOD.log(org.apache.logging.log4j.Level.WARN, String.format(
                    "Chest core at %s holds more than its capacity in slots %s, possibly because config values were "
                            + "lowered. Those slots are extract-only until they fit again.", worldPosition, result.offendingSlots()));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            ChestCoreIndex.unregister(level, worldPosition);
        }
        registered = false;
        onCapabilitiesChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("data_version", DATA_VERSION);
        tag.put("storage", ChestStorage.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), storage.toContents()).getOrThrow());
        saveStructure(tag);
        tag.put("upgrades", ItemContainerContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),
                ItemContainerContents.fromItems(upgrades.getItems())).getOrThrow());
    }

    private void saveStructure(CompoundTag tag) {
        if (structure != null) {
            tag.put("structure", ChestStructure.CODEC.encodeStart(NbtOps.INSTANCE, structure).getOrThrow());
            tag.put("decorated", BlockPos.CODEC.listOf().encodeStart(NbtOps.INSTANCE, decoratedPositions).getOrThrow());
        }
        tag.putInt("last_size", lastSize);
    }

    /**
     * Clients only receive what they render: the structure, not the contents.
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveStructure(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("upgrades")) {
            ItemContainerContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("upgrades"))
                    .resultOrPartial(error -> ColossalChestsInstance.MOD.log(org.apache.logging.log4j.Level.ERROR,
                            "Could not load chest core upgrades at " + worldPosition + ": " + error))
                    .ifPresent(this::loadUpgrades);
        }
        if (tag.contains("storage")) {
            ChestStorage.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("storage"))
                    .resultOrPartial(error -> ColossalChestsInstance.MOD.log(org.apache.logging.log4j.Level.ERROR,
                            "Could not load chest core contents at " + worldPosition + ": " + error))
                    .ifPresent(storage::loadContents);
        }
        structure = tag.contains("structure") ? ChestStructure.CODEC.parse(NbtOps.INSTANCE, tag.get("structure")).result().orElse(null) : null;
        decoratedPositions = structure != null && tag.contains("decorated", Tag.TAG_LIST)
                ? BlockPos.CODEC.listOf().parse(NbtOps.INSTANCE, tag.get("decorated")).result().map(ImmutableList::copyOf).orElse(ImmutableList.of())
                : List.of();
        lastSize = tag.getInt("last_size");
        applyProfile(true);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        ChestStorage.Contents contents = storage.toContents();
        if (!contents.entries().isEmpty()) {
            components.set(RegistryEntries.COMPONENT_CHEST_CONTENTS.value(), contents);
        }
        if (!upgrades.isEmpty()) {
            components.set(RegistryEntries.COMPONENT_CHEST_UPGRADES.value(), ItemContainerContents.fromItems(upgrades.getItems()));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        loadUpgrades(input.getOrDefault(RegistryEntries.COMPONENT_CHEST_UPGRADES.value(), ItemContainerContents.EMPTY));
        ChestStorage.Contents contents = input.get(RegistryEntries.COMPONENT_CHEST_CONTENTS.value());
        if (contents != null) {
            storage.loadContents(contents);
            applyProfile(false);
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("storage");
        tag.remove("upgrades");
    }

    private void loadUpgrades(ItemContainerContents contents) {
        loadingUpgrades = true;
        upgrades.load(contents.stream().toList());
        upgrades.resize(getMaterialProperties(getBlockState()).upgradeSlots());
        loadingUpgrades = false;
        updateCompression();
    }

    /**
     * Compress while the Compression upgrade is installed. The families come from the level's recipes, looked up
     * when needed, as the level is not known yet while loading.
     */
    private void updateCompression() {
        boolean compress = getUpgradeSet().has(ChestUpgrades.COMPRESSION);
        if (compress != storage.isCompressing()) {
            storage.setCompression(compress ? this::getCompressionFamilies : null);
        }
    }

    private CompressionFamilies getCompressionFamilies() {
        return level != null ? CompressionFamiliesCache.get(level) : CompressionFamilies.EMPTY;
    }
}
