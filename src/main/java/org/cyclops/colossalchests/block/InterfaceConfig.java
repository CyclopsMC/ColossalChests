package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link Interface}.
 * @author rubensworks
 *
 */
public class InterfaceConfig extends BlockConfig {

    public InterfaceConfig(ChestMaterial material) {
        super(
                ColossalChests._instance,
            "interface_" + material.getName(),
                eConfig -> new Interface(Block.Properties.of()
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
