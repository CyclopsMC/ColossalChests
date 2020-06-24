package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldWriter;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.colossalchests.tileentity.TileInterface;
import org.cyclops.cyclopscore.block.BlockTile;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;

import javax.annotation.Nullable;

/**
 * Part of the Colossal Blood Chest multiblock structure.
 * @author rubensworks
 *
 */
public class Interface extends BlockTile implements CubeDetector.IDetectionListener, IBlockChestMaterial {

    public static final BooleanProperty ENABLED = ColossalChest.ENABLED;

    private final ChestMaterial material;

    public Interface(Block.Properties properties, ChestMaterial material) {
        super(properties, TileInterface::new);
        this.material = material;

        material.setBlockInterface(this);

        this.setDefaultState(this.stateContainer.getBaseState()
                .with(ENABLED, false));
    }

    @Override
    public String getTranslationKey() {
        String baseKey = super.getTranslationKey();
        return baseKey.substring(0, baseKey.lastIndexOf('_'));
    }

    @Override
    public ChestMaterial getMaterial() {
        return material;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public boolean isToolEffective(BlockState state, ToolType tool) {
        return ColossalChest.isToolEffectiveShared(this.material, state, tool);
    }

    @Override
    public BlockRenderType getRenderType(BlockState blockState) {
        return blockState.get(ENABLED) ? BlockRenderType.ENTITYBLOCK_ANIMATED : super.getRenderType(blockState);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, IBlockReader blockReader, BlockPos blockPos) {
        return blockState.get(ENABLED);
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos,
                                    EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState blockState, ILightReader world, BlockPos pos, IFluidState fluidState) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        ColossalChest.triggerDetector(this.material, world, pos, true, placer instanceof PlayerEntity ? (PlayerEntity) placer : null);
    }

    @Override
    public void onBlockAdded(BlockState blockStateNew, World world, BlockPos blockPos, BlockState blockStateOld, boolean isMoving) {
        super.onBlockAdded(blockStateNew, world, blockPos, blockStateOld, isMoving);
        if(!world.captureBlockSnapshots && blockStateNew.getBlock() != blockStateOld.getBlock() && !blockStateNew.get(ENABLED)) {
            ColossalChest.triggerDetector(this.material, world, blockPos, true, null);
        }
    }

    @Override
    public void onPlayerDestroy(IWorld world, BlockPos blockPos, BlockState blockState) {
        if(blockState.get(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.onPlayerDestroy(world, blockPos, blockState);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if(world.getBlockState(pos).get(ENABLED)) ColossalChest.triggerDetector(material, world, pos, false, null);
        // IForgeBlock.super.onBlockExploded(state, world, pos, explosion);
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        getBlock().onExplosionDestroy(world, pos, explosion);
    }

    @Override
    public void onDetect(IWorldReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            boolean change = !(Boolean) world.getBlockState(location).get(ENABLED);
            ((IWorldWriter) world).setBlockState(location, world.getBlockState(location).with(ENABLED, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            if(change) {
                BlockPos tileLocation = ColossalChest.getCoreLocation(material, world, location);
                TileInterface tile = TileHelpers.getSafeTile(world, location, TileInterface.class).orElse(null);
                if(tile != null && tileLocation != null) {
                    tile.setCorePosition(tileLocation);
                    TileColossalChest core = TileHelpers.getSafeTile(world, tileLocation, TileColossalChest.class).orElse(null);
                    if (core != null) {
                        core.addInterface(location);
                    }
                }
            }
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if(blockState.get(ENABLED)) {
            BlockPos tileLocation = ColossalChest.getCoreLocation(material, world, blockPos);
            if(tileLocation != null) {
                return world.getBlockState(tileLocation).getBlock().
                        onBlockActivated(blockState, world, tileLocation, player, hand, rayTraceResult);
            }
        } else {
            ColossalChest.addPlayerChatError(material, world, blockPos, player, hand);
            return ActionResultType.FAIL;
        }
        return super.onBlockActivated(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public boolean isValidPosition(BlockState blockState, IWorldReader world, BlockPos blockPos) {
        return super.isValidPosition(blockState, world, blockPos) && ColossalChest.canPlace(world, blockPos);
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        if (this.material.isExplosionResistant()) {
            return 10000F;
        }
        return 0;
    }

}
