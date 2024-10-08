package org.cyclops.colossalchests.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChestNeoForge;

/**
 * @author rubensworks
 */
public class ColossalChestNeoForge extends ColossalChest {
    public ColossalChestNeoForge(Properties properties, ChestMaterial material) {
        super(properties, material, BlockEntityColossalChestNeoForge::new);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState blockState, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    protected boolean isCaptureBlockSnapshots(Level level) {
        return level.captureBlockSnapshots;
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        super.onBlockExplodedCommon(state, world, pos, explosion);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return super.getExplosionResistance();
    }
}
