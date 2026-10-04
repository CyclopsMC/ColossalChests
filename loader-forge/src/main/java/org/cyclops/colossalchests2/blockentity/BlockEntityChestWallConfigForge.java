package org.cyclops.colossalchests2.blockentity;

import org.cyclops.cyclopscore.init.IModBase;

/**
 * Forge config for the {@link BlockEntityChestWall}.
 * @author rubensworks
 */
public class BlockEntityChestWallConfigForge<M extends IModBase> extends BlockEntityChestWallConfig<M> {

    public BlockEntityChestWallConfigForge(M mod) {
        super(mod, BlockEntityChestWallForge::new);
        // Forge caches capabilities on block entities, so walls drop their handlers when their chest changes.
        BlockEntityChestCore.capabilityInvalidator = (level, pos) -> {
            if (level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) {
                wall.onCapabilitiesChanged();
            }
        };
    }
}
