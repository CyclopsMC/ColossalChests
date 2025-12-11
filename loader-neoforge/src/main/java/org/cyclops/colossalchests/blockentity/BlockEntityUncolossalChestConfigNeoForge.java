package org.cyclops.colossalchests.blockentity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;

/**
 * @author rubensworks
 */
public class BlockEntityUncolossalChestConfigNeoForge<M extends ModBaseNeoForge<?>> extends BlockEntityUncolossalChestConfig<M> {
    public BlockEntityUncolossalChestConfigNeoForge(M mod) {
        super(mod);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                getInstance(),
                (blockEntity, context) -> VanillaContainerWrapper.of(blockEntity.getInventory())
        );
    }
}
