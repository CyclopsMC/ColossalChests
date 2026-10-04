package org.cyclops.colossalchests2.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for a {@link BlockChestCore} of one material.
 * @author rubensworks
 */
public class BlockChestCoreConfig<M extends IModBase> extends BlockConfigCommon<M> {

    public BlockChestCoreConfig(M mod, ChestMaterial material) {
        super(
                mod,
                "chest_core_" + material.getName(),
                eConfig -> new BlockChestCore(BlockChestWallConfig.createProperties(material), material),
                getDefaultItemConstructor(mod)
        );
    }

}
