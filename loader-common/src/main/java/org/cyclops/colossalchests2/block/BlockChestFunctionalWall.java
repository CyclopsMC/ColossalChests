package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A wall with a function on its own faces. It fits chests of any material.
 * @author rubensworks
 */
public class BlockChestFunctionalWall extends BlockChestWall implements EntityBlock {

    private static final List<BlockChestFunctionalWall> INSTANCES = Lists.newArrayList();

    private final WallType type;

    public BlockChestFunctionalWall(Properties properties, WallType type) {
        super(properties, null, false);
        this.type = type;
        INSTANCES.add(this);
    }

    public static List<BlockChestFunctionalWall> getFunctionalInstances() {
        return Collections.unmodifiableList(INSTANCES);
    }

    public WallType getType() {
        return type;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RegistryEntries.BLOCK_ENTITY_CHEST_WALL.value().create(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Like other walls a click opens the chest, sneaking opens an interface's own settings.
        if (type == WallType.INTERFACE && player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
                IModHelpers.get().getMinecraftHelpers().openMenu(serverPlayer, wall, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (type == WallType.DISPLAY && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
            InteractionResult result = DisplayWallInteractions.useWithoutItem(player, wall);
            if (result != null) {
                return result;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (type == WallType.DISPLAY && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
            ItemInteractionResult result = DisplayWallInteractions.useItemOn(stack, player, wall);
            if (result != null) {
                return result;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return type == WallType.DISPLAY && !level.isClientSide && blockEntityType == RegistryEntries.BLOCK_ENTITY_CHEST_WALL.value()
                ? (l, p, s, be) -> BlockEntityChestWall.serverTick(l, p, s, (BlockEntityChestWall) be) : null;
    }
}
