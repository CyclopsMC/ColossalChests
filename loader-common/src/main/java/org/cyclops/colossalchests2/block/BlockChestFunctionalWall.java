package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.material.ItemMaterialUpgrade;
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
        // A right-click opens the chest, while sneaking opens the settings of an interface, redstone or magnet wall. Display
        // walls handle their own clicks.
        if ((type == WallType.INTERFACE || type == WallType.REDSTONE || type == WallType.MAGNET) && player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
                IModHelpers.get().getMinecraftHelpers().openMenu(serverPlayer, wall, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (type == WallType.DISPLAY && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
            InteractionResult result = DisplayWallInteractions.useWithoutItem(player, wall, hit.getDirection());
            if (result != null) {
                return result;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (type == WallType.DISPLAY && !(stack.getItem() instanceof ItemMaterialUpgrade)
                && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
            ItemInteractionResult result = DisplayWallInteractions.useItemOn(stack, player, wall, hit.getDirection());
            if (result != null) {
                return result;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return type == WallType.REDSTONE;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlockEntityChestWall wall ? wall.getComparatorSignal() : 0;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return type == WallType.REDSTONE;
    }

    /**
     * Weak power to every side, like a trapped chest, so dust and lamps work without a comparator.
     */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return type == WallType.REDSTONE && state.getValue(FORMED) && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall
                ? wall.getRedstoneSignal() : 0;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide || blockEntityType != RegistryEntries.BLOCK_ENTITY_CHEST_WALL.value()) {
            return null;
        }
        return switch (type) {
            case DISPLAY, REDSTONE -> (l, p, s, be) -> BlockEntityChestWall.serverTick(l, p, s, (BlockEntityChestWall) be);
            case MAGNET -> (l, p, s, be) -> MagnetWall.tick(l, p, (BlockEntityChestWall) be);
            default -> null;
        };
    }
}
