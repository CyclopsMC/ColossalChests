package org.cyclops.colossalchests.block;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
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
            BlockRenderLayerMap.putBlock(getInstance(), ChunkSectionLayer.CUTOUT_MIPPED);
        }
    }

}
