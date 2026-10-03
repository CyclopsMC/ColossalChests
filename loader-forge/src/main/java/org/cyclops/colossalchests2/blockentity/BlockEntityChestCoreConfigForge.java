package org.cyclops.colossalchests2.blockentity;

import org.cyclops.cyclopscore.init.ModBaseForge;

/**
 * Forge config for the {@link BlockEntityChestCore}.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfigForge<M extends ModBaseForge<?>> extends BlockEntityChestCoreConfig<M> {

    public BlockEntityChestCoreConfigForge(M mod) {
        super(mod, BlockEntityChestCoreForge::new);
    }
}
