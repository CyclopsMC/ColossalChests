package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * The core of a chest structure, which holds all contents.
 * Broken cores keep their contents as item data, so nothing is ever ejected into the world.
 * @author rubensworks
 */
public class BlockChestCore extends BaseEntityBlock {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    private static final List<BlockChestCore> INSTANCES = Lists.newArrayList();

    private final ChestMaterial material;
    private final MapCodec<BlockChestCore> codec;

    public BlockChestCore(Properties properties, ChestMaterial material) {
        super(properties);
        this.material = material;
        this.codec = simpleCodec(props -> new BlockChestCore(props, material));
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
        INSTANCES.add(this);
    }

    public static List<BlockChestCore> getInstances() {
        return Collections.unmodifiableList(INSTANCES);
    }

    public ChestMaterial getMaterial() {
        return material;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    /**
     * Formed members are drawn by the core's giant chest instead of as blocks.
     */
    public static RenderShape getFormedRenderShape(BlockState state) {
        return state.getValue(FORMED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    /**
     * Formed members must not hide the faces of neighbouring blocks, as the giant chest does not cover them while its lid is open.
     */
    public static VoxelShape getFormedOcclusionShape(BlockState state, VoxelShape unformedShape) {
        return state.getValue(FORMED) ? Shapes.empty() : unformedShape;
    }

    /**
     * Formed members let light through, so blocks next to an open lid are not drawn dark.
     */
    public static int getFormedLightBlock(BlockState state, int unformedLightBlock) {
        return state.getValue(FORMED) ? 0 : unformedLightBlock;
    }

    public static boolean getFormedPropagatesSkylightDown(BlockState state, boolean unformedPropagates) {
        return state.getValue(FORMED) || unformedPropagates;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return getFormedRenderShape(state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getFormedOcclusionShape(state, super.getOcclusionShape(state, level, pos));
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return getFormedLightBlock(state, super.getLightBlock(state, level, pos));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return getFormedPropagatesSkylightDown(state, super.propagatesSkylightDown(state, level, pos));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value(),
                level.isClientSide ? BlockEntityChestCore::clientTick : BlockEntityChestCore::serverTick);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BlockEntityChestCore core) {
            core.dissolve();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof BlockEntityChestCore core) {
            core.requestValidation();
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Creative mode skips drops, but a core with contents must never vanish.
        if (!level.isClientSide && player.isCreative() && level.getBlockEntity(pos) instanceof BlockEntityChestCore core
                && !core.getStorage().toContents().entries().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.applyComponents(core.collectComponents());
            popResourceFromFace(level, pos, getFaceTowards(pos, player), stack);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        // Like the default, but drops pop out on the player's side, so they cannot fall into the chest interior.
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        if (level instanceof ServerLevel serverLevel) {
            Direction face = getFaceTowards(pos, player);
            for (ItemStack drop : getDrops(state, serverLevel, pos, blockEntity, player, tool)) {
                popResourceFromFace(level, pos, face, drop);
            }
            state.spawnAfterBreak(serverLevel, pos, tool, true);
        }
    }

    /**
     * @return The side of the block closest to the player's eyes.
     */
    public static Direction getFaceTowards(BlockPos pos, Player player) {
        Vec3 offset = player.getEyePosition().subtract(Vec3.atCenterOf(pos));
        return Direction.getNearest(offset.x, offset.y, offset.z);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlockEntityChestCore core ? core.getComparatorSignal() : 0;
    }
}
