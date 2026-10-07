package org.cyclops.colossalchests2.block;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.cyclops.colossalchests2.client.render.ItemRendererUncolossalChest;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * NeoForge config for the {@link BlockUncolossalChest}, whose item renders like the block.
 * @author rubensworks
 */
public class BlockUncolossalChestConfigNeoForge<M extends ModBase<?>> extends BlockUncolossalChestConfig<M> {

    public BlockUncolossalChestConfigNeoForge(M mod) {
        super(mod);
        if (mod.getModHelpers().getMinecraftHelpers().isClientSide()) {
            mod.getModEventBus().addListener(this::registerClientExtensions);
        }
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ItemRendererUncolossalChest();
                }
                return renderer;
            }
        }, getItemInstance());
    }
}
