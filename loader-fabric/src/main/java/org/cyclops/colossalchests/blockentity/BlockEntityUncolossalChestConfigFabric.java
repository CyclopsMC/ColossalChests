package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
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
    public void onRegistryRegistered() {
        super.onRegistryRegistered();
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> ContainerStorage.of(blockEntity.getInventory(), context), getInstance());
    }
}
