package org.cyclops.colossalchests.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class ColossalChestConfigForge<M extends IModBase> extends ColossalChestConfig<M> {

    public ColossalChestConfigForge(M mod, ChestMaterial material) {
        super(
                mod,
            "colossal_chest_" + material.getName(),
                (eConfig, properties) -> new ColossalChestForge(((ColossalChestConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "colossal_chest")
        );
    }

    @Override
    public void onRegistryRegistered() {
        super.onRegistryRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            ItemBlockRenderTypes.setRenderLayer(getInstance(), RenderType.cutoutMipped());
        }
    }

}
