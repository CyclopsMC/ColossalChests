package org.cyclops.colossalchests2.block;

import net.minecraft.world.level.block.Block;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for a {@link BlockChestWall} of one material.
 * @author rubensworks
 */
public class BlockChestWallConfig<M extends IModBase> extends BlockConfigCommon<M> {

    public BlockChestWallConfig(M mod, ChestMaterial material) {
        super(
                mod,
                "chest_wall_" + material.getName(),
                eConfig -> new BlockChestWall(createProperties(material), material),
                getDefaultItemConstructor(mod)
        );
    }

    public static Block.Properties createProperties(ChestMaterial material) {
        Block.Properties properties = Block.Properties.of()
                .strength(material.hardness(), material.defaultBlastResistance())
                .sound(material.soundType())
                .isValidSpawn((state, level, pos, entityType) -> false);
        if (material.needsPickaxe()) {
            properties.requiresCorrectToolForDrops();
        }
        return properties;
    }

}
