package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

/**
 * @author rubensworks
 */
public class BlockEntityInterfaceForge extends BlockEntityInterface {
    public BlockEntityInterfaceForge(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        BlockEntityColossalChestForge core = (BlockEntityColossalChestForge) getCore();
        if (core != null) {
            LazyOptional<T> t = core.getCapability(capability, facing);
            if (t.isPresent()) {
                return t;
            }
        }
        return super.getCapability(capability, facing);
    }
}
