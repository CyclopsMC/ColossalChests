package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestConfigFabric<M extends ModBaseFabric> extends BlockEntityColossalChestConfig<M> {
    public BlockEntityColossalChestConfigFabric(M mod) {
        super(mod);
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        // If this would cause performance issues, we could implement a custom Storage instead of InventoryStorage that makes better use of Storage's capabilities.
        // If so, also use this in BlockEntityInterfaceConfigFabric
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> InventoryStorage.of(blockEntity.getInventory(), context), getInstance());
    }
}
