package org.cyclops.colossalchests.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfigNeoForge<M extends IModBase> extends ChestWallConfig<M> {

    public ChestWallConfigNeoForge(M mod, ChestMaterial material) {
        super(
                mod,
            "chest_wall_" + material.getName(),
                (eConfig, properties) -> new ChestWallNeoForge(((ChestWallConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "chest_wall")
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
