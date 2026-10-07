package org.cyclops.colossalchests2.block;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import org.cyclops.colossalchests2.client.render.ItemRendererUncolossalChest;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * Fabric config for the {@link BlockUncolossalChest}, whose item renders like the block.
 * @author rubensworks
 */
public class BlockUncolossalChestConfigFabric<M extends ModBaseFabric<?>> extends BlockUncolossalChestConfig<M> {

    public BlockUncolossalChestConfigFabric(M mod) {
        super(mod);
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            registerItemRenderer();
        }
    }

    private void registerItemRenderer() {
        ItemRendererUncolossalChest[] renderer = new ItemRendererUncolossalChest[1];
        BuiltinItemRendererRegistry.INSTANCE.register(getItemInstance(), (stack, mode, poseStack, buffers, light, overlay) -> {
            if (renderer[0] == null) {
                renderer[0] = new ItemRendererUncolossalChest();
            }
            renderer[0].renderByItem(stack, mode, poseStack, buffers, light, overlay);
        });
    }
}
