package org.cyclops.colossalchests2.blockentity;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
import org.cyclops.colossalchests2.inventory.ContainerInterface;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.storage.DisplayStats;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A functional wall, giving automation access to its chest under the wall's rules.
 * Only an Interface has settings: its filter and direction. A Display wall keeps the type it shows, and its stats
 * for clients.
 * @author rubensworks
 */
public class BlockEntityChestWall extends BlockEntity implements MenuProvider {

    public static final int FILTER_SLOTS = 9;
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
    private ItemStack displayed = ItemStack.EMPTY;
    private DisplayStats displayStats = DisplayStats.EMPTY;
    private final Map<UUID, Long> lastInserts = Maps.newHashMap();

    public BlockEntityChestWall(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
     * @return The type a Display wall shows, empty if none.
     */
    public ItemStack getDisplayed() {
        return displayed;
    }

    public void setDisplayed(ItemStack type) {
        displayed = type.isEmpty() ? ItemStack.EMPTY : type.copyWithCount(1);
        lastInserts.clear();
        setChanged();
        updateDisplayStats(true);
    }

    /**
     * @return What a Display wall shows about its type, as last synced on clients.
     */
    public DisplayStats getDisplayStats() {
        return displayStats;
    }

    /**
     * @param player A player.
     * @param gameTime The current game time.
     * @return If the player's previous insert was recent enough to make this click a double click. Records this click.
     */
    public boolean recordInsertClick(Player player, long gameTime) {
        Long previous = lastInserts.put(player.getUUID(), gameTime);
        return previous != null && gameTime - previous <= DOUBLE_CLICK_TICKS;
    }

    /**
     * Recompute a Display wall's stats and sync them to clients when they changed.
     * @param force If clients are updated even without changes.
     */
    public void updateDisplayStats(boolean force) {
        if (level == null || level.isClientSide) {
            return;
        }
        DisplayStats stats = getCore().map(core -> DisplayStats.of(core.getStorage(), displayed)).orElse(DisplayStats.EMPTY);
        if (force || !stats.equals(displayStats)) {
            displayStats = stats;
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
        if (!displayed.isEmpty()) {
            tag.put("displayed", displayed.save(registries));
        }
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
        displayed = tag.contains("displayed") ? ItemStack.parseOptional(registries, tag.getCompound("displayed")) : ItemStack.EMPTY;
        if (tag.contains("display_stats")) {
            displayStats = DisplayStats.fromTag(tag.getCompound("display_stats"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Clients only need what a Display wall shows.
        CompoundTag tag = new CompoundTag();
        if (!displayed.isEmpty()) {
            tag.put("displayed", displayed.save(registries));
        }
        tag.put("display_stats", displayStats.toTag());
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
        return new ContainerInterface(id, inventory, this);
    }
}
