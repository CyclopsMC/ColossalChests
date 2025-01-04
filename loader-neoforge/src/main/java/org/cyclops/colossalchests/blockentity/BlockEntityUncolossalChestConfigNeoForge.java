package org.cyclops.colossalchests.blockentity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
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
                Capabilities.ItemHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> new InvWrapper(blockEntity.getInventory())
        );
    }
}
