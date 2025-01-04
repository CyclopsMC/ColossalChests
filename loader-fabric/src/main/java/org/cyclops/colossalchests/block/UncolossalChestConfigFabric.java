package org.cyclops.colossalchests.block;

import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigFabric<M extends ModBaseFabric> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigFabric(M mod) {
        super(mod);
    }

    // TODO: register custom item special renderer once Fabric supports this. Then we can modify the items model JSON file.
}
