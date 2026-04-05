package org.cyclops.colossalchests.block;

import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class ColossalChestConfigFabric<M extends IModBase> extends ColossalChestConfig<M> {

    public ColossalChestConfigFabric(M mod, ChestMaterial material) {
        super(
                mod,
            "colossal_chest_" + material.getName(),
                (eConfig, properties) -> new ColossalChestFabric(((ColossalChestConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "colossal_chest")
        );
    }

}
