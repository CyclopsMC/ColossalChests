package org.cyclops.colossalchests.block;

import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import org.cyclops.colossalchests.client.render.blockentity.ItemStackTileEntityUncolossalChestRender;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigNeoForge<M extends ModBaseNeoForge<?>> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigNeoForge(M mod) {
        super(mod);
        if (mod.getModHelpers().getMinecraftHelpers().isClientSide()) {
            mod.getModEventBus().addListener((RegisterSpecialModelRendererEvent event) -> event.register(getResourceKey().location(), ItemStackTileEntityUncolossalChestRender.Unbaked.MAP_CODEC));
        }
    }

}
