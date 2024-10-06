package org.cyclops.colossalchests.block;

import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.cyclops.colossalchests.client.render.blockentity.ItemStackTileEntityUncolossalChestRenderNeoForge;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigNeoForge<M extends ModBase> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigNeoForge(M mod) {
        super(mod);
        if (mod.getModHelpers().getMinecraftHelpers().isClientSide()) {
            mod.getModEventBus().addListener((RegisterClientExtensionsEvent event) -> event.registerItem(new ItemStackTileEntityUncolossalChestRenderNeoForge.ClientItemExtensions(), getItemInstance()));
        }
    }

}
