package org.cyclops.colossalchests.client.render.blockentity;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.client.render.blockentity.ItemStackBlockEntityRendererBase;

/**
 * @author rubensworks
 */
public class ItemStackTileEntityUncolossalChestRenderNeoForge extends ItemStackBlockEntityRendererBase {

    public ItemStackTileEntityUncolossalChestRenderNeoForge() {
        super(() -> new BlockEntityUncolossalChest(BlockPos.ZERO, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.value().defaultBlockState()));
    }

    public static class ClientItemExtensions implements IClientItemExtensions {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return new ItemStackTileEntityUncolossalChestRenderNeoForge();
        }
    }

}
