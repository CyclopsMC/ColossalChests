package org.cyclops.colossalchests.block;

import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfigFabric<M extends IModBase> extends ChestWallConfig<M> {

    public ChestWallConfigFabric(M mod, ChestMaterial material) {
        super(
                mod,
            "chest_wall_" + material.getName(),
                (eConfig, properties) -> new ChestWallFabric(((ChestWallConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "chest_wall")
        );
    }

    @Override
    public void onRegistryRegistered() {
        super.onRegistryRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            BlockRenderLayerMap.putBlock(getInstance(), ChunkSectionLayer.CUTOUT);
        }
    }

}
