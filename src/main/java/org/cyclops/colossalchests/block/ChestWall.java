package org.cyclops.colossalchests.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;

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
        MinecraftForge.EVENT_BUS.addListener(this::onLivingSpawn);
    }

    public void onLivingSpawn(LivingSpawnEvent.CheckSpawn event) {
        // Only isValidSpawn is insufficient in some cases, so we add this forceful check as well.
        if (event.getSpawnReason() != MobSpawnType.CHUNK_GENERATION && event.getEntity().getBlockStateOn().getBlock() == this) {
            event.setResult(Event.Result.DENY);
        }
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return blockState.getValue(ENABLED) ? RenderShape.ENTITYBLOCK_ANIMATED : super.getRenderShape(blockState);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockReader, BlockPos blockPos) {
        return blockState.getValue(ENABLED);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState blockState, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        ColossalChest.triggerDetector(this.material, world, pos, true, placer instanceof Player ? (Player) placer : null);
    }

    @Override
    public void onPlace(BlockState blockStateNew, Level world, BlockPos blockPos, BlockState blockStateOld, boolean isMoving) {
        super.onPlace(blockStateNew, world, blockPos, blockStateOld, isMoving);
        if(!world.captureBlockSnapshots && blockStateNew.getBlock() != blockStateOld.getBlock() && !blockStateNew.getValue(ENABLED)) {
            ColossalChest.triggerDetector(this.material, world, blockPos, true, null);
        }
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.destroy(world, blockPos, blockState);
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        if(world.getBlockState(pos).getValue(ENABLED)) ColossalChest.triggerDetector(material, world, pos, false, null);
        // IForgeBlock.super.onBlockExploded(state, world, pos, explosion);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        wasExploded(world, pos, explosion);
    }

    @Override
    public void onDetect(LevelReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            boolean change = !world.getBlockState(location).getValue(ENABLED);
            ((LevelWriter) world).setBlock(location, world.getBlockState(location).setValue(ENABLED, valid), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
            if(change) {
                BlockEntityColossalChest.detectStructure(world, location, size, valid, originCorner);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult rayTraceResult) {
        if(blockState.getValue(ENABLED)) {
            BlockPos tileLocation = ColossalChest.getCoreLocation(material, world, blockPos);
            if(tileLocation != null) {
                return world.getBlockState(tileLocation).getBlock().
                        use(blockState, world, tileLocation, player, hand, rayTraceResult);
            }
        } else {
            ColossalChest.addPlayerChatError(material, world, blockPos, player, hand);
            return InteractionResult.FAIL;
        }
        return super.use(blockState, world, blockPos, player, hand, rayTraceResult);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader world, BlockPos blockPos) {
        return super.canSurvive(blockState, world, blockPos) && ColossalChest.canPlace(world, blockPos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
        if (this.material.isExplosionResistant()) {
            return 10000F;
        }
        return 0;
    }

}
