package org.cyclops.colossalchests2.multiblock;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tracks the loaded cores per level, so walls can find the core they belong to without storing it.
 * Only used on the server.
 * @author rubensworks
 */
public final class ChestCoreIndex {

    private static final Map<Level, Set<BlockPos>> CORES = new WeakHashMap<>();

    private ChestCoreIndex() {
    }

    public static void register(Level level, BlockPos pos) {
        CORES.computeIfAbsent(level, l -> Sets.newHashSet()).add(pos.immutable());
    }

    public static void unregister(Level level, BlockPos pos) {
        Set<BlockPos> cores = CORES.get(level);
        if (cores != null) {
            cores.remove(pos);
        }
    }

    /**
     * Ask every loaded core that could have a structure through the given position to revalidate.
     * @param level The level.
     * @param pos A changed position.
     */
    public static void requestValidationNear(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }
        Set<BlockPos> cores = CORES.get(level);
        if (cores == null) {
            return;
        }
        int range = GeneralConfig.HARD_MAX_SIZE - 1;
        for (BlockPos core : cores) {
            if (Math.abs(core.getX() - pos.getX()) <= range && Math.abs(core.getY() - pos.getY()) <= range
                    && Math.abs(core.getZ() - pos.getZ()) <= range
                    && level.getBlockEntity(core) instanceof BlockEntityChestCore coreEntity) {
                coreEntity.requestValidation();
            }
        }
    }

    /**
     * @param level The level.
     * @param pos A position.
     * @return The formed core whose shell contains the position.
     */
    public static Optional<BlockEntityChestCore> findFormedCore(Level level, BlockPos pos) {
        Set<BlockPos> cores = CORES.get(level);
        if (cores == null) {
            return Optional.empty();
        }
        int range = GeneralConfig.HARD_MAX_SIZE - 1;
        for (BlockPos core : cores) {
            if (Math.abs(core.getX() - pos.getX()) <= range && Math.abs(core.getY() - pos.getY()) <= range
                    && Math.abs(core.getZ() - pos.getZ()) <= range
                    && level.getBlockEntity(core) instanceof BlockEntityChestCore coreEntity
                    && coreEntity.getStructure() != null && coreEntity.getStructure().isOnShell(pos)) {
                return Optional.of(coreEntity);
            }
        }
        return Optional.empty();
    }

}
