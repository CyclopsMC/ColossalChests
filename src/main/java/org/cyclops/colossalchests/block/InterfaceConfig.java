package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
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
                eConfig -> new Interface(Block.Properties.of(Material.STONE)
                        .strength(5.0F)
                        .sound(SoundType.WOOD)
                        .harvestLevel(0) // Wood tier
                        .noOcclusion(),
                        material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties()
                        .tab(ColossalChests._instance.getDefaultItemGroup()), material)
        );
    }
    
}
