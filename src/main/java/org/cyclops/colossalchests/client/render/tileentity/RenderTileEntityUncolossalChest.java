package org.cyclops.colossalchests.client.render.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

import java.util.function.Supplier;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
@OnlyIn(Dist.CLIENT)
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

}
