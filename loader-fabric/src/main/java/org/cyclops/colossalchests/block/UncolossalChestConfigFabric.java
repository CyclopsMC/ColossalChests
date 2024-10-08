package org.cyclops.colossalchests.block;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import org.cyclops.colossalchests.client.render.blockentity.ItemStackTileEntityUncolossalChestRenderFabric;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigFabric<M extends ModBaseFabric> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigFabric(M mod) {
        super(
                mod,
                "uncolossal_chest",
                eConfig -> new UncolossalChest(((UncolossalChestConfig<M>) eConfig).getProperties()),
                (eConfig, block) -> new BlockItem(block, new Item.Properties())
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();

        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            Wrapper<ItemStackTileEntityUncolossalChestRenderFabric> renderer = new Wrapper<>();
            BuiltinItemRendererRegistry.INSTANCE.register(getItemInstance(), (stack, mode, matrices, vertexConsumers, light, overlay) -> {
                if (renderer.get() == null) {
                    renderer.set(new ItemStackTileEntityUncolossalChestRenderFabric());
                }
                renderer.get().renderByItem(stack, mode, matrices, vertexConsumers, light, overlay);
            });
        }
    }
}
