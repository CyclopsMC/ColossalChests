package org.cyclops.colossalchests2.multiblock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.cyclops.colossalchests2.GeneralConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Explains why the chest blocks around a position do not form a chest, so players can fix their build.
 * The intended structure is taken to be the bounding box of all chest blocks connected to the position.
 * @author rubensworks
 */
public final class StructureDiagnosis {

    private StructureDiagnosis() {
    }

    /**
     * @param view The world.
     * @param start A wall or core position.
     * @param maxSizeForMaterial Gives the maximum size for a material id.
     * @return The diagnosis.
     */
    public static Result diagnose(StructureView view, BlockPos start, MaxSize maxSizeForMaterial) {
        StructureView.Member startMember = view.getMember(start);
        if (startMember == null) {
            return Result.EMPTY;
        }

        // Connected chest blocks, at most a full structure away in each direction.
        int reach = GeneralConfig.HARD_MAX_SIZE;
        Set<BlockPos> members = Sets.newHashSet();
        List<BlockPos> cores = Lists.newArrayList();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start.immutable());
        members.add(start.immutable());
        BlockPos min = start;
        BlockPos max = start;
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            StructureView.Member member = view.getMember(pos);
            if (member.core()) {
                cores.add(pos);
            }
            min = BlockPos.min(min, pos);
            max = BlockPos.max(max, pos);
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (Math.abs(next.getX() - start.getX()) < reach && Math.abs(next.getY() - start.getY()) < reach
                        && Math.abs(next.getZ() - start.getZ()) < reach
                        && view.isLoaded(next) && !members.contains(next) && view.getMember(next) != null) {
                    members.add(next);
                    queue.add(next);
                }
            }
        }

        Object material = cores.size() == 1 ? view.getMember(cores.get(0)).material() : getMaterial(view, members, startMember);
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        int size = Math.max(sizeX, Math.max(sizeY, sizeZ));
        int maxSize = material == StructureView.Member.ANY_MATERIAL ? GeneralConfig.HARD_MAX_SIZE
                : Math.min(maxSizeForMaterial.get(material), GeneralConfig.HARD_MAX_SIZE);

        List<BlockPos> missing = Lists.newArrayList();
        List<BlockPos> wrongMaterial = Lists.newArrayList();
        List<BlockPos> obstructions = Lists.newArrayList();
        boolean cube = sizeX == sizeY && sizeY == sizeZ;
        if (cube && size >= GeneralConfig.MIN_SIZE && size <= maxSize) {
            ChestStructure structure = new ChestStructure(min, size);
            for (BlockPos pos : BlockPos.betweenClosed(min, structure.max())) {
                if (!view.isLoaded(pos)) {
                    continue;
                }
                if (structure.isOnShell(pos)) {
                    StructureView.Member member = view.getMember(pos);
                    if (member == null) {
                        missing.add(pos.immutable());
                    } else if (!member.fits(material)) {
                        wrongMaterial.add(pos.immutable());
                    }
                } else if (!view.isEmpty(pos)) {
                    obstructions.add(pos.immutable());
                }
            }
        }
        return new Result(sizeX, sizeY, sizeZ, maxSize, ImmutableList.copyOf(cores),
                ImmutableList.copyOf(missing), ImmutableList.copyOf(wrongMaterial), ImmutableList.copyOf(obstructions));
    }

    /**
     * @return The material of the first member that has one, as functional walls fit any material.
     */
    private static Object getMaterial(StructureView view, Set<BlockPos> members, StructureView.Member startMember) {
        if (startMember.material() != StructureView.Member.ANY_MATERIAL) {
            return startMember.material();
        }
        for (BlockPos pos : members) {
            Object material = view.getMember(pos).material();
            if (material != StructureView.Member.ANY_MATERIAL) {
                return material;
            }
        }
        return StructureView.Member.ANY_MATERIAL;
    }

    /**
     * Gives the maximum structure size of a material.
     */
    @FunctionalInterface
    public interface MaxSize {
        int get(Object material);
    }

    /**
     * Why a structure does not form. Shape problems are reported first, as the block lists are only
     * computed for a cube of an allowed size.
     * @param sizeX The bounding box size along x.
     * @param sizeY The bounding box size along y.
     * @param sizeZ The bounding box size along z.
     * @param maxSize The maximum size of the material.
     * @param cores The cores among the connected blocks.
     * @param missing Shell positions without a chest block.
     * @param wrongMaterial Shell positions with a block of another material.
     * @param obstructions Interior positions that are not empty.
     */
    public record Result(int sizeX, int sizeY, int sizeZ, int maxSize, List<BlockPos> cores, List<BlockPos> missing,
                         List<BlockPos> wrongMaterial, List<BlockPos> obstructions) {

        public static final Result EMPTY = new Result(0, 0, 0, 0, List.of(), List.of(), List.of(), List.of());

        public Problem getProblem() {
            if (sizeX == 0) {
                return Problem.NONE;
            }
            if (cores.isEmpty()) {
                return Problem.NO_CORE;
            }
            if (cores.size() > 1) {
                return Problem.MULTIPLE_CORES;
            }
            if (sizeX != sizeY || sizeY != sizeZ) {
                return Problem.NOT_CUBE;
            }
            if (sizeX < GeneralConfig.MIN_SIZE) {
                return Problem.TOO_SMALL;
            }
            if (sizeX > maxSize) {
                return Problem.TOO_LARGE;
            }
            if (!missing.isEmpty() || !wrongMaterial.isEmpty() || !obstructions.isEmpty()) {
                return Problem.BLOCKS;
            }
            return Problem.NONE;
        }

        /**
         * @return The positions to highlight for the player.
         */
        public List<BlockPos> getProblemPositions() {
            return switch (getProblem()) {
                case MULTIPLE_CORES -> cores;
                case BLOCKS -> ImmutableList.<BlockPos>builder().addAll(missing).addAll(wrongMaterial).addAll(obstructions).build();
                default -> List.of();
            };
        }

        @Nullable
        public BlockPos getCore() {
            return cores.size() == 1 ? cores.get(0) : null;
        }
    }

    public enum Problem {
        /**
         * Nothing found that prevents formation, for example because the structure is about to form.
         */
        NONE,
        NO_CORE,
        MULTIPLE_CORES,
        NOT_CUBE,
        TOO_SMALL,
        TOO_LARGE,
        BLOCKS
    }

}
