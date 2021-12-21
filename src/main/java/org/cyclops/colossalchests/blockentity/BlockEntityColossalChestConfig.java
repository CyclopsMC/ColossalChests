package org.cyclops.colossalchests.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityColossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;

import java.util.stream.Collectors;

/**
 * Config for the {@link BlockEntityColossalChest}.
 * @author rubensworks
 *
 */
public class BlockEntityColossalChestConfig extends BlockEntityConfig<BlockEntityColossalChest> {

    public BlockEntityColossalChestConfig() {
        super(
                ColossalChests._instance,
                "colossal_chest",
                (eConfig) -> new BlockEntityType<>(BlockEntityColossalChest::new,
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
