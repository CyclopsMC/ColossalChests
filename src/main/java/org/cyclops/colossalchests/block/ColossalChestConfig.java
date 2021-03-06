package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraftforge.fml.config.ModConfig;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class ColossalChestConfig extends BlockConfig {

    @ConfigurableProperty(namedId="colossal_chest", category = "machine", comment = "The maximum size a colossal chest can have.", isCommandable = true, configLocation = ModConfig.Type.SERVER)
    public static int maxSize = 20;

    @ConfigurableProperty(namedId="colossal_chest", category = "general", comment = "If the chest should visually open when someone uses it.", isCommandable = true, configLocation = ModConfig.Type.CLIENT)
    public static boolean chestAnimation = true;

    public ColossalChestConfig(ChestMaterial material) {
        super(
                ColossalChests._instance,
            "colossal_chest_" + material.getName(),
                eConfig -> new ColossalChest(Block.Properties.create(Material.ROCK)
                        .hardnessAndResistance(5.0F)
                        .sound(SoundType.WOOD)
                        .harvestLevel(0) // Wood tier
                        .notSolid(),
                        material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties()
                        .group(ColossalChests._instance.getDefaultItemGroup()), material)
        );
    }
    
}
