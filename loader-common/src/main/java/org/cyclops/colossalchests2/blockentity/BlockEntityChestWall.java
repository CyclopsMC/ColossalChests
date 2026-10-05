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
import org.cyclops.colossalchests2.capability.WallAccess;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;
import org.cyclops.colossalchests2.inventory.ContainerInterface;
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
 * Only an Interface has settings: its filter and direction. A Display wall keeps the type it shows on each face,
 * which faces are hidden, and its stats for clients.
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
    private int hiddenFaces;
    private final Map<UUID, InsertClick> lastInserts = Maps.newHashMap();
    private final Container displayedContainer = new DisplayedContainer();

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
            case DISPLAY -> WallAccess.OPEN;
        };
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
     * @return If a Display wall shows nothing on the face, and acts like a plain wall there.
     */
    public boolean isFaceHidden(Direction face) {
        return (hiddenFaces & (1 << face.ordinal())) != 0;
    }

    /**
     * Show or hide a face of a Display wall. The last shown face of a formed chest can not be hidden, so its settings
     * stay reachable.
     * @param face A face.
     * @param hidden If it is hidden.
     * @return If it changed.
     */
    public boolean setFaceHidden(Direction face, boolean hidden) {
        if (isFaceHidden(face) == hidden
                || (hidden && getDisplayFaces().stream().noneMatch(other -> other != face && !isFaceHidden(other)))) {
            return false;
        }
        setHiddenFaces(hiddenFaces ^ (1 << face.ordinal()));
        return true;
    }

    public int getHiddenFaces() {
        return hiddenFaces;
    }

    private void setHiddenFaces(int hiddenFaces) {
        this.hiddenFaces = hiddenFaces;
        setChanged();
        updateDisplayStats(true);
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
        tag.putInt("hidden_faces", hiddenFaces);
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
        hiddenFaces = tag.getInt("hidden_faces");
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
        return getWallType() == WallType.DISPLAY ? new ContainerDisplay(id, inventory, this) : new ContainerInterface(id, inventory, this);
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
