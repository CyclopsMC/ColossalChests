package org.cyclops.colossalchests.blockentity;

import org.cyclops.cyclopscore.init.ModBaseForge;

/**
 * @author rubensworks
 */
public class BlockEntityInterfaceConfigForge<M extends ModBaseForge> extends BlockEntityInterfaceConfig<M> {
    public BlockEntityInterfaceConfigForge(M mod) {
        super(mod, BlockEntityInterfaceForge::new);
    }
}
