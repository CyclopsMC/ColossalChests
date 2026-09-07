package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.inventory.InventoryUncolossalChestFabric;

/**
 * @author rubensworks
 */
public class BlockEntityUncolossalChestFabric extends BlockEntityUncolossalChest {

    public BlockEntityUncolossalChestFabric(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    protected InventoryUncolossalChestFabric createInventory() {
        return new InventoryUncolossalChestFabric(this, 5, 64);
    }
}
