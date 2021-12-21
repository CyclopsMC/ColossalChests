package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;

import javax.annotation.Nullable;

/**
 * Part of the Colossal Blood Chest multiblock structure.
 * @author rubensworks
 *
 */
public class ChestWall extends Block implements CubeDetector.IDetectionListener, IBlockChestMaterial {

    public static final BooleanProperty ENABLED = ColossalChest.ENABLED;

    private final ChestMaterial material;

    public ChestWall(Block.Properties properties, ChestMaterial material) {
        super(properties);
        this.material = material;

        material.setBlockWall(this);

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ENABLED, false));
    }

    @Override
    public String getDescriptionId() {
        String baseKey = super.getDescriptionId();
        return baseKey.substring(0, baseKey.lastIndexOf('_'));
    }

    @Override
    public ChestMaterial getMaterial() {
        return material;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public boolean isToolEffective(BlockState state, ToolType tool) {
        return ColossalChest.isToolEffectiveShared(this.material, state, tool);
    }

    @Override
    public BlockRenderType getRenderShape(BlockState blockState) {
        return blockState.getValue(ENABLED) ? BlockRenderType.ENTITYBLOCK_ANIMATED : super.getRenderShape(blockState);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, IBlockReader blockReader, BlockPos blockPos) {
        return blockState.getValue(ENABLED);
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos,
                                    EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState blockState, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        ColossalChest.triggerDetector(this.material, world, pos, true, placer instanceof PlayerEntity ? (PlayerEntity) placer : null);
    }

    @Override
    public void onPlace(BlockState blockStateNew, World world, BlockPos blockPos, BlockState blockStateOld, boolean isMoving) {
        super.onPlace(blockStateNew, world, blockPos, blockStateOld, isMoving);
        if(!world.captureBlockSnapshots && blockStateNew.getBlock() != blockStateOld.getBlock() && !blockStateNew.getValue(ENABLED)) {
            ColossalChest.triggerDetector(this.material, world, blockPos, true, null);
        }
    }

    @Override
    public void destroy(IWorld world, BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.destroy(world, blockPos, blockState);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if(world.getBlockState(pos).getValue(ENABLED)) ColossalChest.triggerDetector(material, world, pos, false, null);
        // IForgeBlock.super.onBlockExploded(state, world, pos, explosion);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        getBlock().wasExploded(world, pos, explosion);
    }

    @Override
    public void onDetect(IWorldReader world, BlockPos location, Vector3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            boolean change = !world.getBlockState(location).getValue(ENABLED);
            ((IWorldWriter) world).setBlock(location, world.getBlockState(location).setValue(ENABLED, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            if(change) {
                TileColossalChest.detectStructure(world, location, size, valid, originCorner);
            }
        }
    }

    @Override
    public ActionResultType use(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(blockState.getValue(ENABLED)) {
            BlockPos tileLocation = ColossalChest.getCoreLocation(material, world, blockPos);
            if(tileLocation != null) {
                return world.getBlockState(tileLocation).getBlock().
                        use(blockState, world, tileLocation, player, hand, rayTraceResult);
            }
        } else {
            ColossalChest.addPlayerChatError(material, world, blockPos, player, hand);
            return ActionResultType.FAIL;
        }
        return super.use(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public boolean canSurvive(BlockState blockState, IWorldReader world, BlockPos blockPos) {
        return super.canSurvive(blockState, world, blockPos) && ColossalChest.canPlace(world, blockPos);
    }

    @Override
    public float getExplosionResistance(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        if (this.material.isExplosionResistant()) {
            return 10000F;
        }
        return 0;
    }

}
