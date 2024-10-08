package org.cyclops.colossalchests.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;
import org.cyclops.cyclopscore.block.BlockWithEntityCommon;
import org.cyclops.cyclopscore.block.multi.CubeDetector;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntityCommon;
import org.cyclops.cyclopscore.helper.IModHelpers;

import java.util.function.BiFunction;

/**
 * Part of the Colossal Blood Chest multiblock structure.
 * @author rubensworks
 *
 */
public class Interface extends BlockWithEntityCommon implements CubeDetector.IDetectionListener, IBlockChestMaterial {

    public static final BooleanProperty ENABLED = ColossalChest.ENABLED;

    protected final ChestMaterial material;
    public final MapCodec<Interface> codec;

    public Interface(Block.Properties properties, ChestMaterial material, BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntityCommon> blockEntitySupplier) {
        super(properties, blockEntitySupplier);
        this.material = material;
        this.codec = BlockBehaviour.simpleCodec((props) -> new Interface(props, material, blockEntitySupplier));

        material.setBlockInterface(this);

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ENABLED);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
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
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        ColossalChest.triggerDetector(this.material, world, pos, true, placer instanceof Player ? (Player) placer : null);
    }

    @Override
    public void onPlace(BlockState blockStateNew, Level world, BlockPos blockPos, BlockState blockStateOld, boolean isMoving) {
        super.onPlace(blockStateNew, world, blockPos, blockStateOld, isMoving);
        if(!isCaptureBlockSnapshots(world) && blockStateNew.getBlock() != blockStateOld.getBlock() && !blockStateNew.getValue(ENABLED)) {
            ColossalChest.triggerDetector(this.material, world, blockPos, true, null);
        }
    }

    protected boolean isCaptureBlockSnapshots(Level level) {
        return false;
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos blockPos, BlockState blockState) {
        if(blockState.getValue(ENABLED)) ColossalChest.triggerDetector(material, world, blockPos, false, null);
        super.destroy(world, blockPos, blockState);
    }

    @Override
    public void onDetect(LevelReader world, BlockPos location, Vec3i size, boolean valid, BlockPos originCorner) {
        Block block = world.getBlockState(location).getBlock();
        if(block == this) {
            boolean change = !(Boolean) world.getBlockState(location).getValue(ENABLED);
            ((LevelWriter) world).setBlock(location, world.getBlockState(location).setValue(ENABLED, valid), IModHelpers.get().getMinecraftHelpers().getBlockNotifyClient());
            if(change) {
                BlockPos tileLocation = ColossalChest.getCoreLocation(material, world, location);
                BlockEntityInterface tile = IModHelpers.get().getBlockEntityHelpers().get(world, location, BlockEntityInterface.class).orElse(null);
                if(tile != null && tileLocation != null) {
                    tile.setCorePosition(tileLocation);
                    BlockEntityColossalChest core = IModHelpers.get().getBlockEntityHelpers().get(world, tileLocation, BlockEntityColossalChest.class).orElse(null);
                    if (core != null) {
                        core.addInterface(location);
                    }
                }
            }
        }
    }

    public void onBlockExplodedCommon(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        if(world.getBlockState(pos).getValue(ENABLED)) ColossalChest.triggerDetector(material, world, pos, false, null);
        // IForgeBlock.super.onBlockExploded(state, world, pos, explosion);
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        wasExploded(world, pos, explosion);
    }

    @Override
    public float getExplosionResistance() {
        if (this.material.isExplosionResistant()) {
            return 10000F;
        }
        return 0;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState blockState, Level world, BlockPos blockPos, Player player, BlockHitResult rayTraceResult) {
        if(blockState.getValue(ENABLED)) {
            BlockPos tileLocation = ColossalChest.getCoreLocation(material, world, blockPos);
            if(tileLocation != null) {
                return world.getBlockState(tileLocation).useWithoutItem(world, player, rayTraceResult.withPosition(tileLocation));
            }
        } else {
            ColossalChest.addPlayerChatError(material, world, blockPos, player);
            return InteractionResult.FAIL;
        }
        return super.useWithoutItem(blockState, world, blockPos, player, rayTraceResult);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader world, BlockPos blockPos) {
        return super.canSurvive(blockState, world, blockPos) && ColossalChest.canPlace(world, blockPos);
    }

}
