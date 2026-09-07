package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * @author rubensworks
 */
public class BlockEntityUncolossalChestConfigFabric<M extends ModBaseFabric> extends BlockEntityUncolossalChestConfig<M> {
    public BlockEntityUncolossalChestConfigFabric(M mod) {
        super(mod);
    }

    @Override
    protected BlockEntityType.BlockEntitySupplier<? extends BlockEntityUncolossalChest> getBlockEntitySupplier() {
        return BlockEntityUncolossalChestFabric::new;
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        // Pass no direction: our inventories expose all slots to all sides,
        // and a direction would make Fabric allocate a new sided wrapper per slot on every lookup.
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> InventoryStorage.of(blockEntity.getInventory(), null), getInstance());
    }
}
