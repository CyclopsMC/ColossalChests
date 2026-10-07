package org.cyclops.colossalchests2.block;

import net.minecraft.world.level.block.Block;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.function.Supplier;

/**
 * Config for a {@link BlockChestWall} of one material.
 * @author rubensworks
 */
public class BlockChestWallConfig<M extends IModBase> extends BlockConfigCommon<M> {

    /**
     * @param mod The mod registering the block, named chest_wall_[material name].
     * @param material The material.
     * @param properties Creates the block properties, such as hardness and sound.
     */
    public BlockChestWallConfig(M mod, ChestMaterial material, Supplier<Block.Properties> properties) {
        super(
                mod,
                "chest_wall_" + material.getName(),
                eConfig -> new BlockChestWall(properties.get(), material),
                getDefaultItemConstructor(mod)
        );
    }

}
