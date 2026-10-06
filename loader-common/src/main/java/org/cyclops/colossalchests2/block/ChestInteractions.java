package org.cyclops.colossalchests2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.config.ChestTablesLoader;
import org.cyclops.colossalchests2.material.ItemMaterialUpgrade;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.colossalchests2.multiblock.LevelStructureView;
import org.cyclops.colossalchests2.multiblock.StructureDiagnosis;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * Right-clicking a wall or core opens the chest when formed, and otherwise explains why it does not form.
 * @author rubensworks
 */
public final class ChestInteractions {

    /**
     * Highlighted problem positions, so huge chests do not flood the client with particles.
     */
    public static final int MAX_HIGHLIGHTS = 64;
    private static final DustParticleOptions HIGHLIGHT = new DustParticleOptions(new Vector3f(1.0F, 0.1F, 0.1F), 2.0F);

    private ChestInteractions() {
    }

    /**
     * With an item in hand, an unformed chest lets the item be used, so walls can be placed against walls
     * while building. A formed chest still opens, like a vanilla chest, unless a Material Upgrade is used on it.
     */
    public static ItemInteractionResult useItemOn(ItemStack stack, BlockState state) {
        return !stack.isEmpty() && (!state.getValue(BlockChestCore.FORMED) || stack.getItem() instanceof ItemMaterialUpgrade)
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static InteractionResult use(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        Optional<BlockEntityChestCore> core = findFormedCore(state, level, pos);
        if (core.isPresent()) {
            core.get().openMenu(serverPlayer);
            core.get().warnIfOverCapacity(serverPlayer);
        } else {
            explain(serverPlayer, (ServerLevel) level, pos);
        }
        return InteractionResult.CONSUME;
    }

    /**
     * @return The formed chest that a click on a wall or core opens.
     */
    public static Optional<BlockEntityChestCore> findFormedCore(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(BlockChestCore.FORMED)) {
            return Optional.empty();
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityChestCore core) {
            return Optional.of(core).filter(BlockEntityChestCore::isFormed);
        }
        return ChestCoreIndex.findFormedCore(level, pos);
    }

    /**
     * Tell the player why the chest at a position does not form, and highlight the blocks at fault.
     */
    public static StructureDiagnosis.Result explain(ServerPlayer player, ServerLevel level, BlockPos pos) {
        StructureDiagnosis.Result result = StructureDiagnosis.diagnose(new LevelStructureView(level, pos), pos,
                material -> ChestTablesLoader.get().getMaterial((ResourceLocation) material).maxSize());
        player.displayClientMessage(getMessage(result), true);
        List<BlockPos> positions = result.getProblemPositions();
        for (BlockPos problem : positions.subList(0, Math.min(positions.size(), MAX_HIGHLIGHTS))) {
            level.sendParticles(player, HIGHLIGHT, true, problem.getX() + 0.5, problem.getY() + 0.5, problem.getZ() + 0.5,
                    8, 0.3, 0.3, 0.3, 0);
        }
        return result;
    }

    public static Component getMessage(StructureDiagnosis.Result result) {
        String prefix = "chest.colossalchests2.diagnosis.";
        return switch (result.getProblem()) {
            case NONE -> Component.translatable(prefix + "none");
            case NO_CORE -> Component.translatable(prefix + "no_core");
            case MULTIPLE_CORES -> Component.translatable(prefix + "multiple_cores", result.cores().size());
            case NOT_CUBE -> Component.translatable(prefix + "not_cube", result.sizeX(), result.sizeY(), result.sizeZ());
            case TOO_SMALL -> Component.translatable(prefix + "too_small");
            case TOO_LARGE -> Component.translatable(prefix + "too_large", result.sizeX(), result.maxSize());
            case BLOCKS -> Component.translatable(prefix + "blocks",
                    result.missing().size(), result.wrongMaterial().size(), result.obstructions().size());
        };
    }

}
