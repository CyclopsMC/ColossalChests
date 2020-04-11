package org.cyclops.colossalchests.tileentity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.client.render.tileentity.RenderTileEntityColossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;

import java.util.stream.Collectors;

/**
 * Config for the {@link TileColossalChest}.
 * @author rubensworks
 *
 */
public class TileColossalChestConfig extends TileEntityConfig<TileColossalChest> {

    public TileColossalChestConfig() {
        super(
                ColossalChests._instance,
                "colossal_chest",
                (eConfig) -> new TileEntityType<>(TileColossalChest::new,
                        ChestMaterial.VALUES.stream()
                                .map(ChestMaterial::getBlockCore)
                                .collect(Collectors.toSet()), null)
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ColossalChests._instance.getProxy().registerRenderer(getInstance(), RenderTileEntityColossalChest::new);
    }

}
