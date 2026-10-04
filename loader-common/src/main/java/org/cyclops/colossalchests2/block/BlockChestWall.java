package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;

import java.util.Collections;
import java.util.List;

/**
 * A wall of a chest structure. Changes to walls or their surroundings make nearby cores revalidate.
 * @author rubensworks
 */
public class BlockChestWall extends Block {

    public static final BooleanProperty FORMED = BlockChestCore.FORMED;

    /**
     * If plain walls expose the item storage of their chest, which interface walls will take over.
     */
    public static final boolean EXPOSES_CAPABILITIES = true;

    private static final List<BlockChestWall> INSTANCES = Lists.newArrayList();

    private final ChestMaterial material;

    public BlockChestWall(Properties properties, ChestMaterial material) {
        super(properties);
        this.material = material;
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
        INSTANCES.add(this);
    }

    public static List<BlockChestWall> getInstances() {
        return Collections.unmodifiableList(INSTANCES);
    }

    public ChestMaterial getMaterial() {
        return material;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ChestInteractions.useItemOn(stack, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return ChestInteractions.use(state, level, pos, player);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return BlockChestCore.getFormedOcclusionShape(state, super.getOcclusionShape(state, level, pos));
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return BlockChestCore.getFormedLightBlock(state, super.getLightBlock(state, level, pos));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return BlockChestCore.getFormedPropagatesSkylightDown(state, super.propagatesSkylightDown(state, level, pos));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!state.is(oldState.getBlock())) {
            ChestCoreIndex.requestValidationNear(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            ChestCoreIndex.requestValidationNear(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        ChestCoreIndex.requestValidationNear(level, pos);
    }
}
