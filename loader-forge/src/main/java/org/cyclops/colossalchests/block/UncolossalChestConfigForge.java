package org.cyclops.colossalchests.block;

import org.cyclops.cyclopscore.init.ModBaseForge;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigForge<M extends ModBaseForge> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigForge(M mod) {
        super(mod);
    }

    // TODO: register custom item special renderer once Forge supports this. Then we can modify the items model JSON file.
}
