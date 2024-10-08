package org.cyclops.colossalchests.client.render.blockentity;

import net.minecraft.core.BlockPos;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.client.render.blockentity.ItemStackBlockEntityRendererBase;

/**
 * @author rubensworks
 */
public class ItemStackTileEntityUncolossalChestRenderFabric extends ItemStackBlockEntityRendererBase {

    public ItemStackTileEntityUncolossalChestRenderFabric() {
        super(() -> new BlockEntityUncolossalChest(BlockPos.ZERO, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value().defaultBlockState()));
    }

}
