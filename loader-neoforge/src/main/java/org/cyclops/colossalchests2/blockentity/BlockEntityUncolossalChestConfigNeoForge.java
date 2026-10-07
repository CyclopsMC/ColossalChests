package org.cyclops.colossalchests2.blockentity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * NeoForge config for the {@link BlockEntityUncolossalChest}, exposing its slots as an item handler.
 * @author rubensworks
 */
public class BlockEntityUncolossalChestConfigNeoForge<M extends ModBase<?>> extends BlockEntityUncolossalChestConfig<M> {

    public BlockEntityUncolossalChestConfigNeoForge(M mod) {
        super(mod, BlockEntityUncolossalChest::new);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    protected void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, getInstance(), (chest, side) -> new InvWrapper(chest));
    }
}
