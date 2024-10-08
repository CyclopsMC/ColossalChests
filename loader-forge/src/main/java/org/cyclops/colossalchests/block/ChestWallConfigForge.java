package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
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
                eConfig -> new ChestWallForge(((ChestWallConfig<M>) eConfig).getProperties(), material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties(), material)
        );
    }

}
