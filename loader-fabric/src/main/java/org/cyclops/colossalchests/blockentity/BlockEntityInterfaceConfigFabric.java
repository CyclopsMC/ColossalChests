package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * @author rubensworks
 */
public class BlockEntityInterfaceConfigFabric<M extends ModBaseFabric> extends BlockEntityInterfaceConfig<M> {
    public BlockEntityInterfaceConfigFabric(M mod) {
        super(mod, BlockEntityInterface::new);
    }

    @Override
    public void onRegistryRegistered() {
        super.onRegistryRegistered();
        // Pass no direction: our inventories expose all slots to all sides,
        // and a direction would make Fabric allocate a new sided wrapper per slot on every lookup.
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            BlockEntityColossalChest core = blockEntity.getCore();
            if (core != null) {
                return ContainerStorage.of(core.getInventory(), null);
            }
            return null;
        }, getInstance());
    }
}
