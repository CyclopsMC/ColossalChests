package org.cyclops.colossalchests.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

import java.util.function.Supplier;

/**
 * @author rubensworks
 */
@OnlyIn(Dist.CLIENT)
public class ItemStackTileEntityUncolossalChestRender extends ItemStackTileEntityRenderer {

    private final TileUncolossalChest tile;

    public ItemStackTileEntityUncolossalChestRender() {
        this.tile = new TileUncolossalChest();
    }

    @Override
    public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        TileEntityRendererDispatcher.instance.renderItem(this.tile, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

}
