package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorageForge;
import org.cyclops.colossalchests2.capability.ItemHandlerLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Forge functional wall, exposing its item handler while its chest is formed.
 * @author rubensworks
 */
public class BlockEntityChestWallForge extends BlockEntityChestWall {

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();

    public BlockEntityChestWallForge(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public void onCapabilitiesChanged() {
        super.onCapabilitiesChanged();
        itemHandler.invalidate();
        itemHandler = LazyOptional.empty();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        onCapabilitiesChanged();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !isRemoved()) {
            if (!itemHandler.isPresent()) {
                Optional<ItemHandlerLogic> logic = getItemHandlerLogic();
                if (logic.isEmpty()) {
                    return LazyOptional.empty();
                }
                IItemHandler handler = new ItemHandlerChestStorageForge(logic.get());
                itemHandler = LazyOptional.of(() -> handler);
            }
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }
}
