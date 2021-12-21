package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
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
                        .noOcclusion(),
                        material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties()
                        .tab(ColossalChests._instance.getDefaultItemGroup()), material)
        );
    }
    
}
