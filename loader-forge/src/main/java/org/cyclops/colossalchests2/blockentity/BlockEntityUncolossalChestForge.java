package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Forge uncolossal chest, exposing its slots as an item handler.
 * @author rubensworks
 */
public class BlockEntityUncolossalChestForge extends BlockEntityUncolossalChest {

    private LazyOptional<InvWrapper> itemHandler = LazyOptional.of(() -> new InvWrapper(this));

    public BlockEntityUncolossalChestForge(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && !isRemoved()) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> new InvWrapper(this));
    }
}
