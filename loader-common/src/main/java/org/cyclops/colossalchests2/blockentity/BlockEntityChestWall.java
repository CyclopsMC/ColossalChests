package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A functional wall, giving automation access to its chest under the wall's rules.
 * Only an Interface has settings: its filter and direction.
 * @author rubensworks
 */
public class BlockEntityChestWall extends BlockEntity implements MenuProvider {

    public static final int FILTER_SLOTS = 9;

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
     * @return Item handler semantics for automation through this wall, while its chest is formed.
     */
    public Optional<ItemHandlerLogic> getItemHandlerLogic() {
        return getCore().map(core -> new ItemHandlerLogic(core.getStorage(), getAccess()));
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
