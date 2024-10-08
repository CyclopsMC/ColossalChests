package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class ColossalChestConfigForge<M extends IModBase> extends ColossalChestConfig<M> {

    public ColossalChestConfigForge(M mod, ChestMaterial material) {
        super(
                mod,
            "colossal_chest_" + material.getName(),
                eConfig -> new ColossalChestForge(((ColossalChestConfig<M>) eConfig).getProperties(), material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties(), material)
        );
    }

}
