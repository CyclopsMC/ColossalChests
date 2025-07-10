package org.cyclops.colossalchests.block;

import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfigForge<M extends IModBase> extends ChestWallConfig<M> {

    public ChestWallConfigForge(M mod, ChestMaterial material) {
        super(
                mod,
            "chest_wall_" + material.getName(),
                (eConfig, properties) -> new ChestWallForge(((ChestWallConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "chest_wall")
        );
    }

}
