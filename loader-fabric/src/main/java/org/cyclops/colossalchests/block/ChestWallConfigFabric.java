package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfigFabric<M extends IModBase> extends ChestWallConfig<M> {

    public ChestWallConfigFabric(M mod, ChestMaterial material) {
        super(
                mod,
            "chest_wall_" + material.getName(),
                eConfig -> new ChestWallFabric(((ChestWallConfig<M>) eConfig).getProperties(), material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties(), material)
        );
    }

}
