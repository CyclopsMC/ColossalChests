package org.cyclops.colossalchests.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestConfigFabric<M extends ModBaseFabric> extends BlockEntityColossalChestConfig<M> {
    public BlockEntityColossalChestConfigFabric(M mod) {
        super(mod);
    }

    @Override
    protected BlockEntityType.BlockEntitySupplier<? extends BlockEntityColossalChest> getBlockEntitySupplier() {
        return BlockEntityColossalChestFabric::new;
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        // If this would cause performance issues, we could implement a custom Storage instead of InventoryStorage that makes better use of Storage's capabilities.
        // If so, also use this in BlockEntityInterfaceConfigFabric
        // Pass no direction: our inventories expose all slots to all sides,
        // and a direction would make Fabric allocate a new sided wrapper per slot on every lookup.
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> InventoryStorage.of(blockEntity.getInventory(), null), getInstance());
    }
}
