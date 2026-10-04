package org.cyclops.colossalchests2.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.cyclops.colossalchests2.client.render.ChestRenderLayersFabric;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * Fabric config for the {@link BlockEntityChestCore}, exposing item storage on formed cores.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfigFabric<M extends ModBaseFabric<?>> extends BlockEntityChestCoreConfig<M> {

    public BlockEntityChestCoreConfigFabric(M mod) {
        super(mod, BlockEntityChestCoreFabric::new);
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            ChestRenderLayersFabric.register();
        }
        ItemStorage.SIDED.registerForBlockEntity((core, side) -> core.isFormed() ? ((BlockEntityChestCoreFabric) core).getFabricStorage() : null, getInstance());
    }
}
