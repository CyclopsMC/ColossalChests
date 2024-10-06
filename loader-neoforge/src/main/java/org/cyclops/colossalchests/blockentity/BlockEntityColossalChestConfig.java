package org.cyclops.colossalchests.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
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
        ColossalChests._instance.getModEventBus().addListener(this::registerCapabilities);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ColossalChests._instance.getProxy().registerRenderer(getInstance(), RenderTileEntityColossalChest::new);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> new InvWrapper(blockEntity.getInventory())
        );
    }

}
