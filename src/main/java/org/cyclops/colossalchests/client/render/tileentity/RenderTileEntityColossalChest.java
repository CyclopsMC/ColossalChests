package org.cyclops.colossalchests.client.render.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityModel;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityColossalChest extends RenderTileEntityModel<TileColossalChest, ModelChest> {

	/**
     * Make a new instance.
     * @param model The model to render.
     * @param texture The texture to render the model with.
     */
    public RenderTileEntityColossalChest(ModelChest model, ResourceLocation texture) {
        super(model, texture);
    }

    @Override
    protected void preRotate(TileColossalChest chestTile) {
        if(chestTile.isStructureComplete()) {
            Vec3 renderOffset = chestTile.getRenderOffset();
            GlStateManager.translate(-renderOffset.xCoord, renderOffset.yCoord, renderOffset.zCoord);
        }
        GlStateManager.translate(0.5F, 0.3F, 0.5F);
        float size = chestTile.getSizeSingular() * 1.125F;
        GlStateManager.scale(size, size, size);
    }

    @Override
    protected void postRotate(TileColossalChest tile) {
        GlStateManager.translate(-0.5F, 0, -0.5F);
    }

    @Override
    protected void renderModel(TileColossalChest chestTile, ModelChest model, float partialTick, int destroyStage) {
        if(chestTile.isStructureComplete()) {
            GlStateManager.pushMatrix();
            float lidangle = chestTile.prevLidAngle + (chestTile.lidAngle - chestTile.prevLidAngle) * partialTick;
            lidangle = 1.0F - lidangle;
            lidangle = 1.0F - lidangle * lidangle * lidangle;
            model.chestLid.rotateAngleX = -(lidangle * (float) Math.PI / 2.0F);
            GlStateManager.translate(0, -0.0625F * 8, 0);
            model.renderAll();
            GlStateManager.popMatrix();
        }
    }
}
