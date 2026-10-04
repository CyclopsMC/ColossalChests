package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorageForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge chest core, exposing its item handler while formed.
 * Interface walls expose their own handlers through their block entities.
 * @author rubensworks
 */
public class BlockEntityChestCoreForge extends BlockEntityChestCore {

    private LazyOptional<IItemHandler> itemHandler = LazyOptional.empty();

    public BlockEntityChestCoreForge(BlockPos pos, BlockState state) {
        super(RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value(), pos, state);
    }

    @Override
    protected void onCapabilitiesChanged() {
        super.onCapabilitiesChanged();
        itemHandler.invalidate();
        itemHandler = LazyOptional.empty();
    }

    @Override
    public AABB getRenderBoundingBox() {
        return getRenderBounds();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && isFormed() && !isRemoved()) {
            if (!itemHandler.isPresent()) {
                IItemHandler handler = new ItemHandlerChestStorageForge(getItemHandlerLogic());
                itemHandler = LazyOptional.of(() -> handler);
            }
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }
}
