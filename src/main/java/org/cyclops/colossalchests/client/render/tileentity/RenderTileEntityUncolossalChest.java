package org.cyclops.colossalchests.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

import java.util.function.Supplier;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityUncolossalChest extends RenderTileEntityChestBase<TileUncolossalChest> {

    public RenderTileEntityUncolossalChest(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
        super(tileEntityRendererDispatcher);
    }

    @Override
    protected Direction getDirection(TileUncolossalChest tile) {
        return tile.getRotation();
    }

    @Override
    public void render(TileUncolossalChest tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.push();
        matrixStack.translate(0.325F, 0F, 0.325F);
        float size = 0.3F * 1.125F;
        matrixStack.scale(size, size, size);
        super.render(tile, partialTicks, matrixStack, renderTypeBuffer, combinedLightIn, combinedOverlayIn);
        matrixStack.pop();
    }

    public static class ItemStackRender extends ItemStackTileEntityRenderer {

        private final Supplier<TileUncolossalChest> tile;

        public ItemStackRender(Supplier<TileUncolossalChest> tile) {
            this.tile = tile;
        }

        @Override
        public void render(ItemStack itemStackIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
            TileEntityRendererDispatcher.instance.renderItem(this.tile.get(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }

    }

}
