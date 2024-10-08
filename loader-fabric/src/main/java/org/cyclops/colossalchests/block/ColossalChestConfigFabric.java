package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
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
                eConfig -> new ColossalChestFabric(((ColossalChestConfig<M>) eConfig).getProperties(), material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties(), material)
        );
    }

}
