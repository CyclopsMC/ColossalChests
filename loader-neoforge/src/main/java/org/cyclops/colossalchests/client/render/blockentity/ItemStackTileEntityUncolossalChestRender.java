package org.cyclops.colossalchests.client.render.blockentity;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.cyclops.colossalchests.RegistryEntriesCommon;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.client.render.blockentity.ItemStackBlockEntityRendererBase;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackTileEntityUncolossalChestRender extends ItemStackBlockEntityRendererBase {

    public ItemStackTileEntityUncolossalChestRender() {
        super(() -> new BlockEntityUncolossalChest(BlockPos.ZERO, RegistryEntriesCommon.BLOCK_UNCOLOSSAL_CHEST.value().defaultBlockState()));
    }

    public static class ClientItemExtensions implements IClientItemExtensions {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return new ItemStackTileEntityUncolossalChestRender();
        }
    }

}
