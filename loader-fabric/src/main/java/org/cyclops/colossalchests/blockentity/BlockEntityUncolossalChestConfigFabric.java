package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * @author rubensworks
 */
public class BlockEntityUncolossalChestConfigFabric<M extends ModBaseFabric> extends BlockEntityUncolossalChestConfig<M> {
    public BlockEntityUncolossalChestConfigFabric(M mod) {
        super(mod);
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> InventoryStorage.of(blockEntity.getInventory(), context), getInstance());
    }
}
