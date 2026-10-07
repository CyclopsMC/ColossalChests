package org.cyclops.colossalchests2.multiblock;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Places complete chests at once.
 * @author rubensworks
 */
public final class ChestBuilder {

    private ChestBuilder() {
    }

    /**
     * @param feet The block the builder stands in.
     * @param facing The horizontal direction the builder looks in.
     * @param size The outer edge length.
     * @return A chest in front of the builder, one block away, centered on its view and level with its feet.
     */
    public static ChestStructure inFront(BlockPos feet, Direction facing, int size) {
        Direction side = facing.getClockWise();
        BlockPos near = feet.relative(facing, 2).relative(side, -(size / 2));
        BlockPos far = near.relative(facing, size - 1).relative(side, size - 1).above(size - 1);
        return new ChestStructure(new BlockPos(Math.min(near.getX(), far.getX()), near.getY(), Math.min(near.getZ(), far.getZ())), size);
    }

    /**
     * @param structure A chest.
     * @param viewer A position to face.
     * @return The middle of the vertical side closest to the viewer.
     */
    public static BlockPos getCorePos(ChestStructure structure, Vec3 viewer) {
        BlockPos center = structure.min().offset(structure.size() / 2, structure.size() / 2, structure.size() / 2);
        int last = structure.size() - 1;
        List<BlockPos> sides = List.of(
                new BlockPos(center.getX(), center.getY(), structure.min().getZ()),
                new BlockPos(center.getX(), center.getY(), structure.min().getZ() + last),
                new BlockPos(structure.min().getX(), center.getY(), center.getZ()),
                new BlockPos(structure.min().getX() + last, center.getY(), center.getZ()));
        return sides.stream().min(Comparator.comparingDouble(pos -> Vec3.atCenterOf(pos).distanceToSqr(viewer))).orElseThrow();
    }

    /**
     * @return If the whole chest is within the world's height and in loaded chunks.
     */
    public static boolean isLoaded(Level level, ChestStructure structure) {
        return level.isInWorldBounds(structure.min()) && level.isInWorldBounds(structure.max())
                && level.hasChunksAt(structure.min(), structure.max());
    }

    /**
     * @return The positions in the way: shell blocks that cannot be replaced and interior blocks that are not air.
     */
    public static List<BlockPos> findObstructions(Level level, ChestStructure structure) {
        List<BlockPos> obstructions = Lists.newArrayList();
        for (BlockPos pos : BlockPos.betweenClosed(structure.min(), structure.max())) {
            BlockState state = level.getBlockState(pos);
            if (structure.isOnShell(pos) ? !state.canBeReplaced() : !state.isAir()) {
                obstructions.add(pos.immutable());
            }
        }
        return obstructions;
    }

    /**
     * Place the walls and core, overwriting anything in the way without dropping it.
     * @return The formed core, or empty if the chest did not form.
     */
    public static Optional<BlockEntityChestCore> build(Level level, ChestStructure structure, ChestMaterial material, BlockPos corePos) {
        for (BlockPos pos : BlockPos.betweenClosed(structure.min(), structure.max())) {
            BlockState state = structure.isOnShell(pos) ? material.getWallBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
            if (!pos.equals(corePos) && level.getBlockState(pos) != state) {
                Clearable.tryClear(level.getBlockEntity(pos));
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        }
        Clearable.tryClear(level.getBlockEntity(corePos));
        level.setBlock(corePos, material.getCoreBlock().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(corePos) instanceof BlockEntityChestCore core) {
            core.validateNow();
            if (core.isFormed()) {
                return Optional.of(core);
            }
        }
        return Optional.empty();
    }

}
