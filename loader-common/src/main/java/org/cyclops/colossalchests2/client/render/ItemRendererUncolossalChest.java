package org.cyclops.colossalchests2.client.render;

import net.minecraft.core.BlockPos;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.client.render.blockentity.ItemStackBlockEntityRendererBase;

/**
 * Renders the uncolossal chest item like the placed block.
 * @author rubensworks
 */
public class ItemRendererUncolossalChest extends ItemStackBlockEntityRendererBase {

    public ItemRendererUncolossalChest() {
        super(() -> new BlockEntityUncolossalChest(BlockPos.ZERO, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value().defaultBlockState()));
    }

}
