package org.cyclops.colossalchests.blockentity;

import com.google.common.collect.Sets;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.RegistryEntriesCommon;
import org.cyclops.colossalchests.client.render.blockentity.RenderTileEntityUncolossalChest;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;

/**
 * Config for the {@link BlockEntityUncolossalChest}.
 * @author rubensworks
 *
 */
public class BlockEntityUncolossalChestConfig extends BlockEntityConfig<BlockEntityUncolossalChest> {

    public BlockEntityUncolossalChestConfig() {
        super(
                ColossalChests._instance,
                "uncolossal_chest",
                (eConfig) -> new BlockEntityType<>(BlockEntityUncolossalChest::new,
                        Sets.newHashSet(RegistryEntriesCommon.BLOCK_UNCOLOSSAL_CHEST.value()), null)
        );
        ColossalChests._instance.getModEventBus().addListener(this::registerCapabilities);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onRegistered() {
        super.onRegistered();
        ColossalChests._instance.getProxy().registerRenderer(getInstance(), RenderTileEntityUncolossalChest::new);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> new InvWrapper(blockEntity.getInventory())
        );
    }
}
