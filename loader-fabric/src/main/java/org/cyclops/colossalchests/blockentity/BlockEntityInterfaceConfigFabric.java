package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
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
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            BlockEntityColossalChest core = blockEntity.getCore();
            if (core != null) {
                return InventoryStorage.of(core.getInventory(), context);
            }
            return null;
        }, getInstance());
    }
}
