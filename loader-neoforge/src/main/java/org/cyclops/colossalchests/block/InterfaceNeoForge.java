package org.cyclops.colossalchests.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;

/**
 * @author rubensworks
 */
public class InterfaceNeoForge extends Interface {
    public InterfaceNeoForge(Properties properties, ChestMaterial material) {
        super(properties, material, BlockEntityInterface::new);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState blockState, BlockAndLightGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    protected boolean canBlockSnapshotsBeCaptured() {
        return true;
    }

    @Override
    protected boolean isCaptureBlockSnapshots(Level level) {
        return level.captureBlockSnapshots;
    }

    @Override
    public void onBlockExploded(BlockState state, ServerLevel world, BlockPos pos, Explosion explosion) {
        super.onBlockExplodedCommon(state, world, pos, explosion);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        return super.getExplosionResistance();
    }
}
