package org.cyclops.colossalchests.client.render.blockentity;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.client.render.blockentity.ItemStackBlockEntityRendererBase;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackTileEntityUncolossalChestRender extends ItemStackBlockEntityRendererBase {

    public ItemStackTileEntityUncolossalChestRender() {
        super(() -> new BlockEntityUncolossalChest(BlockPos.ZERO, RegistryEntries.BLOCK_UNCOLOSSAL_CHEST.defaultBlockState()));
    }

    public static class ItemRenderProperties implements IItemRenderProperties {
        public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
            return new ItemStackTileEntityUncolossalChestRender();
        }
    }

}
