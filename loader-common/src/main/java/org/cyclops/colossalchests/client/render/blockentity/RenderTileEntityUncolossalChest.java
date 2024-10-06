package org.cyclops.colossalchests.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityUncolossalChest extends RenderTileEntityChestBase<BlockEntityUncolossalChest> {

    public RenderTileEntityUncolossalChest(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Direction getDirection(BlockEntityUncolossalChest tile) {
        return tile.getRotation();
    }

    @Override
    public void render(BlockEntityUncolossalChest tile, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.pushPose();
        matrixStack.translate(0.325F, 0F, 0.325F);
        float size = 0.3F * 1.125F;
        matrixStack.scale(size, size, size);
        super.render(tile, partialTicks, matrixStack, renderTypeBuffer, combinedLightIn, combinedOverlayIn);
        matrixStack.popPose();
    }

}
