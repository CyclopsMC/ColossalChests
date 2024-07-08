package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfig extends BlockConfig {

    public ChestWallConfig(ChestMaterial material) {
        super(
                ColossalChests._instance,
            "chest_wall_" + material.getName(),
                eConfig -> new ChestWall(Block.Properties.of()
                        .strength(5.0F)
                        .sound(SoundType.WOOD)
                        .requiresCorrectToolForDrops()
                        .noOcclusion()
                        .isValidSpawn((state, level, pos, entityType) -> false),
                        material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties(), material)
        );
    }

}
