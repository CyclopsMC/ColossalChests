package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
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
                eConfig -> new ChestWall(Block.Properties.of(Material.STONE)
                        .strength(5.0F)
                        .sound(SoundType.WOOD)
                        .noOcclusion(),
                        material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties()
                        .tab(ColossalChests._instance.getDefaultItemGroup()), material)
        );
    }

}
