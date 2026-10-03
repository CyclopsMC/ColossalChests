package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.cyclops.colossalchests2.GeneralConfig;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the hollow cube that a core forms with the walls around it.
 * A valid structure has walls of the core's material on its whole shell, exactly one core (this one)
 * on its shell, an empty interior, and a size between {@link GeneralConfig#MIN_SIZE} and the given maximum.
 * Neighbouring structures and stray walls next to a chest do not prevent formation.
 * @author rubensworks
 */
public final class StructureDetector {

    private StructureDetector() {
    }

    /**
     * @param view The world.
     * @param core The core position.
     * @param maxSize The maximum size allowed for the core's material.
     * @return The detection result. If several cubes are valid, the largest one wins, so a larger candidate
     *         in unloaded chunks makes the result {@link Result#UNLOADED}.
     */
    public static Result detect(StructureView view, BlockPos core, int maxSize) {
        StructureView.Member coreMember = view.getMember(core);
        if (coreMember == null || !coreMember.core()) {
            return Result.INVALID;
        }
        Object material = coreMember.material();
        maxSize = Math.min(maxSize, GeneralConfig.HARD_MAX_SIZE);

        // Contiguous members along each axis bound the possible cube positions.
        int[] neg = new int[3];
        int[] pos = new int[3];
        Direction.Axis[] axes = Direction.Axis.values();
        for (int a = 0; a < 3; a++) {
            neg[a] = run(view, core, Direction.fromAxisAndDirection(axes[a], Direction.AxisDirection.NEGATIVE), material, maxSize - 1);
            pos[a] = run(view, core, Direction.fromAxisAndDirection(axes[a], Direction.AxisDirection.POSITIVE), material, maxSize - 1);
        }

        boolean unloaded = false;
        for (int size = maxSize; size >= GeneralConfig.MIN_SIZE; size--) {
            for (int ox = 0; ox < size; ox++) {
                if (!isAllowed(ox, size, neg[0], pos[0])) {
                    continue;
                }
                for (int oy = 0; oy < size; oy++) {
                    if (!isAllowed(oy, size, neg[1], pos[1])) {
                        continue;
                    }
                    for (int oz = 0; oz < size; oz++) {
                        if (!isAllowed(oz, size, neg[2], pos[2])) {
                            continue;
                        }
                        if (!isEdge(ox, size) && !isEdge(oy, size) && !isEdge(oz, size)) {
                            continue;
                        }
                        ChestStructure candidate = new ChestStructure(core.offset(-ox, -oy, -oz), size);
                        Validity validity = validate(view, candidate, core, material);
                        if (validity == Validity.VALID) {
                            // A larger candidate may become valid once its chunks load, so wait for it.
                            return unloaded ? Result.UNLOADED : Result.valid(candidate);
                        }
                        unloaded |= validity == Validity.UNLOADED;
                    }
                }
            }
        }
        return unloaded ? Result.UNLOADED : Result.INVALID;
    }

    private static int run(StructureView view, BlockPos start, Direction direction, Object material, int limit) {
        int count = 0;
        BlockPos.MutableBlockPos current = start.mutable();
        while (count < limit) {
            current.move(direction);
            if (!view.isLoaded(current)) {
                break;
            }
            StructureView.Member member = view.getMember(current);
            if (member == null || !member.material().equals(material)) {
                break;
            }
            count++;
        }
        return count;
    }

    // The core sits at offset o along an axis of a cube of the given size. Along axes in the plane of the
    // core's face, the whole edge line must be walls; along the axis normal to the face, the core is on the
    // first or last layer and the line crosses the empty interior.
    private static boolean isAllowed(int offset, int size, int neg, int pos) {
        return isEdge(offset, size) || (offset <= neg && size - 1 - offset <= pos);
    }

    private static boolean isEdge(int offset, int size) {
        return offset == 0 || offset == size - 1;
    }

    static Validity validate(StructureView view, ChestStructure structure, BlockPos core, Object material) {
        BlockPos min = structure.min();
        int size = structure.size();
        int last = size - 1;
        BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
        // Unloaded positions only matter if all loaded positions pass.
        boolean unloaded = false;
        for (int dx = 0; dx < size; dx++) {
            for (int dy = 0; dy < size; dy++) {
                for (int dz = 0; dz < size; dz++) {
                    current.set(min.getX() + dx, min.getY() + dy, min.getZ() + dz);
                    if (!view.isLoaded(current)) {
                        unloaded = true;
                        continue;
                    }
                    boolean shell = dx == 0 || dx == last || dy == 0 || dy == last || dz == 0 || dz == last;
                    if (shell) {
                        StructureView.Member member = view.getMember(current);
                        if (member == null || !member.material().equals(material) || (member.core() && !current.equals(core))) {
                            return Validity.INVALID;
                        }
                    } else if (!view.isEmpty(current)) {
                        return Validity.INVALID;
                    }
                }
            }
        }
        return unloaded ? Validity.UNLOADED : Validity.VALID;
    }

    enum Validity {
        VALID,
        INVALID,
        UNLOADED
    }

    /**
     * The outcome of a detection.
     * @param state The state.
     * @param structure The structure if valid.
     */
    public record Result(State state, @Nullable ChestStructure structure) {

        public static final Result INVALID = new Result(State.INVALID, null);
        public static final Result UNLOADED = new Result(State.UNLOADED, null);

        public static Result valid(ChestStructure structure) {
            return new Result(State.VALID, structure);
        }

        public enum State {
            VALID,
            INVALID,
            /**
             * Part of a candidate structure is in an unloaded chunk, so the outcome is unknown.
             */
            UNLOADED
        }
    }

}
