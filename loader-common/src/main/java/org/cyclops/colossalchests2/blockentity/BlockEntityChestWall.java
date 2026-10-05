package org.cyclops.colossalchests2.blockentity;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWall;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.capability.ItemHandlerLogic;
import org.cyclops.colossalchests2.capability.StorageSignals;
import org.cyclops.colossalchests2.capability.WallAccess;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;
import org.cyclops.colossalchests2.inventory.ContainerInterface;
import org.cyclops.colossalchests2.inventory.ContainerRedstone;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.multiblock.ChestShape;
import org.cyclops.colossalchests2.storage.DisplayStats;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A functional wall, giving automation access to its chest under the wall's rules.
 * An Interface has a filter and direction. A Display wall keeps the type it shows on each face, what each face shows,
 * and its stats for clients. A Redstone wall keeps the item type it signals for.
 * @author rubensworks
 */
public class BlockEntityChestWall extends BlockEntity implements MenuProvider {

    public static final int FILTER_SLOTS = 9;
    public static final int FACES = Direction.values().length;
    /**
     * The longest time between two clicks on a Display wall that make a double click.
     */
    public static final int DOUBLE_CLICK_TICKS = 10;
    private static final int DISPLAY_UPDATE_TICKS = 10;
    private static final int SIGNAL_UPDATE_TICKS = 10;

    private final SimpleContainer settings = new SimpleContainer(FILTER_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            onSettingsChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };
    private WallAccess.Mode mode = WallAccess.Mode.BOTH;
    private final ItemStack[] displayed = new ItemStack[FACES];
    private final DisplayStats[] displayStats = new DisplayStats[FACES];
    // A bit per side and option, set when the option is off.
    private int disabledOptions;
    private final Map<UUID, InsertClick> lastInserts = Maps.newHashMap();
    private final Container displayedContainer = new DisplayedContainer();
    // The last signal a Redstone wall emitted, so redstone does not recompute it on every query.
    private int redstoneSignal;
    private final SimpleContainer redstoneTarget = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            BlockEntityChestWall.this.setChanged();
            updateRedstoneSignal();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };

    public BlockEntityChestWall(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(displayed, ItemStack.EMPTY);
        Arrays.fill(displayStats, DisplayStats.EMPTY);
    }

    public BlockEntityChestWall(BlockPos pos, BlockState state) {
        this(RegistryEntries.BLOCK_ENTITY_CHEST_WALL.value(), pos, state);
    }

    public WallType getWallType() {
        return getBlockState().getBlock() instanceof BlockChestFunctionalWall wall ? wall.getType() : WallType.INTERFACE;
    }

    /**
     * @return The filter slots. Each holds at most one item.
     */
    public Container getSettings() {
        return settings;
    }

    public WallAccess.Mode getMode() {
        return mode;
    }

    public void setMode(WallAccess.Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            onSettingsChanged();
        }
    }

    /**
     * @return What automation may do through this wall.
     */
    public WallAccess getAccess() {
        return switch (getWallType()) {
            case INTERFACE -> new WallAccess(mode, settings.getItems(), false);
            case VOID -> new WallAccess(WallAccess.Mode.BOTH, List.of(), true);
            case DISPLAY, REDSTONE -> WallAccess.OPEN;
        };
    }

    /**
     * @return The item type a Redstone wall signals for, empty for the whole chest.
     */
    public Container getRedstoneTarget() {
        return redstoneTarget;
    }

    /**
     * @return The comparator signal of a Redstone wall, 0 while its chest is not formed.
     */
    public int getComparatorSignal() {
        ItemStack target = redstoneTarget.getItem(0);
        return getCore().map(core -> target.isEmpty() ? StorageSignals.getComparatorSignal(core.getStorage())
                : StorageSignals.getComparatorSignal(core.getStorage(), target)).orElse(0);
    }

    /**
     * @return The redstone power a Redstone wall emits to its neighbours, as last updated.
     */
    public int getRedstoneSignal() {
        return redstoneSignal;
    }

    /**
     * Refresh the signal of a Redstone wall: comparators read it again, and neighbours are updated when its power
     * changed.
     */
    public void updateRedstoneSignal() {
        if (level == null || level.isClientSide) {
            return;
        }
        Block block = getBlockState().getBlock();
        int signal = getComparatorSignal();
        if (signal != redstoneSignal) {
            redstoneSignal = signal;
            setChanged();
            level.updateNeighborsAt(worldPosition, block);
        }
        level.updateNeighbourForOutputSignal(worldPosition, block);
    }

    /**
     * @return The formed chest this wall belongs to.
     */
    public Optional<BlockEntityChestCore> getCore() {
        if (level == null || !getBlockState().getValue(BlockChestWall.FORMED)) {
            return Optional.empty();
        }
        return ChestCoreIndex.findFormedCore(level, worldPosition);
    }

    /**
     * @return The formed chest this wall gives automation access to, empty for walls without item access.
     */
    public Optional<BlockEntityChestCore> getExposedCore() {
        return getWallType().exposesItems() ? getCore() : Optional.empty();
    }

    /**
     * @return Item handler semantics for automation through this wall, while its chest is formed.
     */
    public Optional<ItemHandlerLogic> getItemHandlerLogic() {
        return getExposedCore().map(core -> new ItemHandlerLogic(core.getStorage(), getAccess()));
    }

    /**
     * @return The faces of a Display wall on the outside of its formed chest, which can show an item.
     */
    public List<Direction> getDisplayFaces() {
        return getCore().map(core -> ChestShape.getOuterFaces(core.getStructure(), worldPosition)).orElse(List.of());
    }

    /**
     * @param face A face.
     * @return The type a Display wall shows on the face, empty if none.
     */
    public ItemStack getDisplayed(Direction face) {
        return displayed[face.ordinal()];
    }

    public void setDisplayed(Direction face, ItemStack type) {
        displayed[face.ordinal()] = type.isEmpty() ? ItemStack.EMPTY : type.copyWithCount(1);
        lastInserts.clear();
        setChanged();
        updateDisplayStats(true);
    }

    /**
     * @param face A face.
     * @param option An option.
     * @return If the option is on for the face of a Display wall.
     */
    public boolean isEnabled(Direction face, DisplayOption option) {
        return (disabledOptions & getOptionBit(face, option)) == 0;
    }

    /**
     * Turn an option of a face of a Display wall on or off.
     * @param face A face.
     * @param option An option.
     * @param enabled If it is on.
     * @return If it changed.
     */
    public boolean setEnabled(Direction face, DisplayOption option, boolean enabled) {
        if (isEnabled(face, option) == enabled) {
            return false;
        }
        disabledOptions ^= getOptionBit(face, option);
        setChanged();
        updateDisplayStats(true);
        return true;
    }

    /**
     * @param face A face.
     * @return If a Display wall shows nothing on the face, and acts like a plain wall there.
     */
    public boolean isFaceHidden(Direction face) {
        return !isEnabled(face, DisplayOption.SHOWN);
    }

    /**
     * @param face A face.
     * @return The options that are off for the face, a bit per option ordinal.
     */
    public int getDisabledOptions(Direction face) {
        return (disabledOptions >> (face.ordinal() * DisplayOption.values().length)) & ((1 << DisplayOption.values().length) - 1);
    }

    private static int getOptionBit(Direction face, DisplayOption option) {
        return 1 << (face.ordinal() * DisplayOption.values().length + option.ordinal());
    }

    /**
     * @return A container with the type a Display wall shows on each face, by face ordinal, for its menu.
     */
    public Container getDisplayedContainer() {
        return displayedContainer;
    }

    /**
     * @param face A face.
     * @return What a Display wall shows about the type on the face, as last synced on clients.
     */
    public DisplayStats getDisplayStats(Direction face) {
        return displayStats[face.ordinal()];
    }

    /**
     * @param player A player.
     * @param face The clicked face.
     * @param gameTime The current game time.
     * @return If the player's previous insert was on the same face and recent enough to make this click a double
     * click. Records this click.
     */
    public boolean recordInsertClick(Player player, Direction face, long gameTime) {
        InsertClick previous = lastInserts.put(player.getUUID(), new InsertClick(face, gameTime));
        return previous != null && previous.face() == face && gameTime - previous.gameTime() <= DOUBLE_CLICK_TICKS;
    }

    /**
     * Recompute a Display wall's stats and sync them to clients when they changed.
     * @param force If clients are updated even without changes.
     */
    public void updateDisplayStats(boolean force) {
        if (level == null || level.isClientSide) {
            return;
        }
        Optional<BlockEntityChestCore> core = getCore();
        boolean changed = force;
        for (Direction face : Direction.values()) {
            ItemStack type = displayed[face.ordinal()];
            DisplayStats stats = core.map(c -> DisplayStats.of(c.getStorage(), type)).orElse(DisplayStats.EMPTY);
            if (!stats.equals(displayStats[face.ordinal()])) {
                displayStats[face.ordinal()] = stats;
                changed = true;
            }
        }
        if (changed) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityChestWall wall) {
        if (level.getGameTime() % DISPLAY_UPDATE_TICKS == 0 && wall.getWallType() == WallType.DISPLAY) {
            wall.updateDisplayStats(false);
        }
        // Contents changes update the signal right away, this catches the chest forming or breaking.
        if (level.getGameTime() % SIGNAL_UPDATE_TICKS == 0 && wall.getWallType() == WallType.REDSTONE) {
            wall.updateRedstoneSignal();
        }
    }

    private void onSettingsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockEntityChestCore.capabilityInvalidator.invalidate(level, worldPosition);
            onCapabilitiesChanged();
        }
    }

    /**
     * Called when what this wall exposes changes, for loaders that cache capabilities on the block entity.
     */
    public void onCapabilitiesChanged() {
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("mode", mode.name());
        ContainerHelper.saveAllItems(tag, settings.getItems(), registries);
        saveDisplay(tag, registries, false);
        if (!redstoneTarget.getItem(0).isEmpty()) {
            tag.put("redstone_target", redstoneTarget.getItem(0).save(registries));
        }
        tag.putInt("redstone_signal", redstoneSignal);
    }

    private void saveDisplay(CompoundTag tag, HolderLookup.Provider registries, boolean withStats) {
        ListTag faces = new ListTag();
        for (Direction face : Direction.values()) {
            ItemStack type = displayed[face.ordinal()];
            if (!type.isEmpty() || (withStats && !displayStats[face.ordinal()].equals(DisplayStats.EMPTY))) {
                CompoundTag faceTag = new CompoundTag();
                faceTag.putString("face", face.getSerializedName());
                if (!type.isEmpty()) {
                    faceTag.put("item", type.save(registries));
                }
                if (withStats) {
                    faceTag.put("stats", displayStats[face.ordinal()].toTag());
                }
                faces.add(faceTag);
            }
        }
        tag.put("display", faces);
        tag.putInt("disabled_options", disabledOptions);
    }

    private void loadDisplay(CompoundTag tag, HolderLookup.Provider registries) {
        Arrays.fill(displayed, ItemStack.EMPTY);
        Arrays.fill(displayStats, DisplayStats.EMPTY);
        for (Tag entry : tag.getList("display", Tag.TAG_COMPOUND)) {
            CompoundTag faceTag = (CompoundTag) entry;
            Direction face = Direction.byName(faceTag.getString("face"));
            if (face != null) {
                displayed[face.ordinal()] = faceTag.contains("item")
                        ? ItemStack.parseOptional(registries, faceTag.getCompound("item")) : ItemStack.EMPTY;
                if (faceTag.contains("stats")) {
                    displayStats[face.ordinal()] = DisplayStats.fromTag(faceTag.getCompound("stats"));
                }
            }
        }
        disabledOptions = tag.getInt("disabled_options");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        try {
            mode = WallAccess.Mode.valueOf(tag.getString("mode"));
        } catch (IllegalArgumentException e) {
            mode = WallAccess.Mode.BOTH;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(settings.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        for (int i = 0; i < items.size(); i++) {
            settings.getItems().set(i, items.get(i));
        }
        loadDisplay(tag, registries);
        redstoneTarget.getItems().set(0, tag.contains("redstone_target")
                ? ItemStack.parseOptional(registries, tag.getCompound("redstone_target")) : ItemStack.EMPTY);
        redstoneSignal = tag.getInt("redstone_signal");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Clients only need what a Display wall shows.
        CompoundTag tag = new CompoundTag();
        saveDisplay(tag, registries, true);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return switch (getWallType()) {
            case DISPLAY -> new ContainerDisplay(id, inventory, this);
            case REDSTONE -> new ContainerRedstone(id, inventory, this);
            default -> new ContainerInterface(id, inventory, this);
        };
    }

    private record InsertClick(Direction face, long gameTime) {
    }

    private class DisplayedContainer implements Container {

        @Override
        public int getContainerSize() {
            return FACES;
        }

        @Override
        public boolean isEmpty() {
            return Arrays.stream(displayed).allMatch(ItemStack::isEmpty);
        }

        @Override
        public ItemStack getItem(int slot) {
            return displayed[slot];
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return removeItemNoUpdate(slot);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack previous = getItem(slot);
            setItem(slot, ItemStack.EMPTY);
            return previous;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            setDisplayed(Direction.from3DDataValue(slot), stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            BlockEntityChestWall.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(BlockEntityChestWall.this, player);
        }

        @Override
        public void clearContent() {
            for (Direction face : Direction.values()) {
                setDisplayed(face, ItemStack.EMPTY);
            }
        }
    }
}
