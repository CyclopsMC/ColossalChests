package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.render.tileentity.ItemStackTileEntityUncolossalChestRender;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfig extends BlockConfig {

    public UncolossalChestConfig() {
        super(
                ColossalChests._instance,
            "uncolossal_chest",
                eConfig -> new UncolossalChest(Block.Properties.create(Material.ROCK)
                        .hardnessAndResistance(5.0F)
                        .sound(SoundType.WOOD)
                        .harvestLevel(0)),
                getDefaultItemConstructor(ColossalChests._instance, (itemProperties) -> itemProperties.setISTER(() -> ItemStackTileEntityUncolossalChestRender::new))
        );
    }
    
}
