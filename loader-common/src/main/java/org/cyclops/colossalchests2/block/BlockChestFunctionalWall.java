package org.cyclops.colossalchests2.block;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        // A Filtered Interface opens its own settings, other walls and sneaking open the chest.
        if (type == WallType.FILTERED_INTERFACE && !player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
                IModHelpers.get().getMinecraftHelpers().openMenu(serverPlayer, wall, buf -> buf.writeBlockPos(pos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
}
