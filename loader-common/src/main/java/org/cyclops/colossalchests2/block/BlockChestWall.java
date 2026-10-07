package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.multiblock.StructureView;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A wall of a chest structure. Changes to walls or their surroundings make nearby cores revalidate.
 * Addons make functional walls by extending this with {@link #BlockChestWall(Properties)}. Such walls fit chests of any
 * material and are never replaced by the Material Upgrade Tool. They reach their chest with
 * {@link ChestCoreIndex#findFormedCore}, and can draw on the giant chest with
 * {@link org.cyclops.colossalchests2.client.render.ChestOverlays}.
 * @author rubensworks
 */
public class BlockChestWall extends Block {

    public static final BooleanProperty FORMED = BlockChestCore.FORMED;

    private static final List<BlockChestWall> INSTANCES = Lists.newArrayList();

    @Nullable
    private final ChestMaterial material;

    /**
     * A plain wall of a material.
     */
    public BlockChestWall(Properties properties, ChestMaterial material) {
        this(properties, material, true);
    }

    /**
     * A wall without a material, which fits chests of any material.
     */
    public BlockChestWall(Properties properties) {
        this(properties, null, false);
    }

    protected BlockChestWall(Properties properties, @Nullable ChestMaterial material, boolean plain) {
        super(properties);
        this.material = material;
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
        if (plain) {
            INSTANCES.add(this);
        }
    }

    /**
     * @return The plain walls.
     */
    public static List<BlockChestWall> getInstances() {
        return Collections.unmodifiableList(INSTANCES);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (material != null) {
            tooltip.add(material.getLimitsTooltip());
        }
    }

    /**
     * @return The material of a plain wall, null for functional walls.
     */
    @Nullable
    public ChestMaterial getMaterial() {
        return material;
    }

    /**
     * @return If this is a plain wall, which has a material and no function.
     */
    public boolean isPlain() {
        return material != null;
    }

    /**
     * @return The material id for structure detection.
     */
    public Object getMemberMaterial() {
        return material == null ? StructureView.Member.ANY_MATERIAL : material.id();
    }

    /**
     * Called on the server, at most once per tick, after the contents of the formed chest changed. Only called for
     * walls that are not plain.
     * @param state The state of this wall.
     * @param level The level.
     * @param pos The position of this wall.
     * @param core The chest.
     */
    public void onChestContentsChanged(BlockState state, Level level, BlockPos pos, BlockEntityChestCore core) {
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
