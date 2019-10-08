package org.cyclops.colossalchests.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.render.tileentity.RenderTileEntityUncolossalChest;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;
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
                getDefaultItemConstructor(ColossalChests._instance)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ChestModel model = new ChestModel();
        ColossalChests._instance.getProxy().registerRenderer(TileUncolossalChest.class, new RenderTileEntityUncolossalChest(model));
    }
    
}
