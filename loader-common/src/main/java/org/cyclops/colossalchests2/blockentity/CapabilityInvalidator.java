package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Tells the loader that the capabilities at a position changed, for loaders that cache them.
 * @author rubensworks
 */
public interface CapabilityInvalidator {

    CapabilityInvalidator NOOP = (level, pos) -> {};

    void invalidate(Level level, BlockPos pos);

}
