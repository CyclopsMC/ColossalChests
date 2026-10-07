package org.cyclops.colossalchests2.block;

import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for a {@link BlockChestFunctionalWall} of one type.
 * @author rubensworks
 */
public class BlockChestFunctionalWallConfig<M extends IModBase> extends BlockConfigCommon<M> {

    public BlockChestFunctionalWallConfig(M mod, WallType type) {
        super(
                mod,
                type.getRegistryName(),
                eConfig -> new BlockChestFunctionalWall(BuiltInMaterial.IRON.createProperties(), type),
                getDefaultItemConstructor(mod)
        );
    }

}
