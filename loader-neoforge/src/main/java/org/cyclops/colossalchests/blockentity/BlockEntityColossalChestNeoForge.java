package org.cyclops.colossalchests.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.cyclopscore.inventory.SimpleInventoryCommon;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestNeoForge extends BlockEntityColossalChest {
    public BlockEntityColossalChestNeoForge(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
    }

    @Override
    public void setInventory(SimpleInventoryCommon inventory) {
        invalidateCapabilities();
        super.setInventory(inventory);
    }
}
