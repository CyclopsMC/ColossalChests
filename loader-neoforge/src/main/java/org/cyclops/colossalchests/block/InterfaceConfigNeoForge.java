package org.cyclops.colossalchests.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link Interface}.
 * @author rubensworks
 *
 */
public class InterfaceConfigNeoForge<M extends IModBase> extends InterfaceConfig<M> {

    public InterfaceConfigNeoForge(M mod, ChestMaterial material) {
        super(
                mod,
            "interface_" + material.getName(),
                (eConfig, properties) -> new InterfaceNeoForge(((InterfaceConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "interface")
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
