package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.inventory.InventoryColossalChestFabric;
import org.cyclops.colossalchests.inventory.InventoryLargeFabric;
import org.cyclops.cyclopscore.inventory.LargeInventoryCommon;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestFabric extends BlockEntityColossalChest {

    public BlockEntityColossalChestFabric(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    protected InventoryColossalChestFabric createInventory(int size, int stackLimit) {
        return new InventoryColossalChestFabric(this, size, stackLimit);
    }

    @Override
    protected LargeInventoryCommon createInventoryLarge(int size, int stackLimit) {
        return new InventoryLargeFabric(size, stackLimit);
    }
}
