package org.cyclops.colossalchests2.multiblock;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * What the structure detector needs to know about the blocks in a world.
 * @author rubensworks
 */
public interface StructureView {

    /**
     * @param pos A position.
     * @return If the position can be read without loading chunks.
     */
    boolean isLoaded(BlockPos pos);

    /**
     * @param pos A position.
     * @return The structure member at the position, or null if it is not a chest wall or core, or if it is
     *         already part of another formed chest.
     */
    @Nullable
    Member getMember(BlockPos pos);

    /**
     * @param pos A position.
     * @return If the position is empty, as required inside the structure.
     */
    boolean isEmpty(BlockPos pos);

    /**
     * A block that can be part of a structure.
     * @param material The material id, or {@link #ANY_MATERIAL} for walls that fit any material.
     * @param core If it is a core, otherwise a wall.
     */
    record Member(Object material, boolean core) {

        /**
         * The material of functional walls, which fit in a structure of any material.
         */
        public static final Object ANY_MATERIAL = "any";

        /**
         * @param structureMaterial The material of a structure.
         * @return If this member may be part of a structure of that material.
         */
        public boolean fits(Object structureMaterial) {
            return material == ANY_MATERIAL || material.equals(structureMaterial);
        }
    }

}
