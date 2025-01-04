package org.cyclops.colossalchests.block;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link Interface}.
 * @author rubensworks
 *
 */
public class InterfaceConfigFabric<M extends IModBase> extends InterfaceConfig<M> {

    public InterfaceConfigFabric(M mod, ChestMaterial material) {
        super(
                mod,
            "interface_" + material.getName(),
                (eConfig, properties) -> new InterfaceFabric(((InterfaceConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "interface")
        );
    }

    @Override
    public void onRegistryRegistered() {
        super.onRegistryRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            BlockRenderLayerMap.INSTANCE.putBlock(getInstance(), RenderType.cutoutMipped());
        }
    }

}
