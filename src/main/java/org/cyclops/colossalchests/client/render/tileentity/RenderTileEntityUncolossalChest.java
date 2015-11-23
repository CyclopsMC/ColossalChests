package org.cyclops.colossalchests.client.render.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityModel;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityUncolossalChest extends RenderTileEntityModel<TileUncolossalChest, ModelChest> {

	/**
     * Make a new instance.
     * @param model The model to render.
     */
    public RenderTileEntityUncolossalChest(ModelChest model) {
        super(model, null);
    }

    @Override
    protected void preRotate(TileUncolossalChest chestTile) {
        GlStateManager.translate(0.5F, 0.83F, 0.5F);
        float size = 0.3F * 1.125F;
        GlStateManager.scale(size, size, size);
    }

    @Override
    protected void postRotate(TileUncolossalChest tile) {
        GlStateManager.translate(-0.5F, 0, -0.5F);
    }

    @Override
    protected void renderModel(TileUncolossalChest chestTile, ModelChest model, float partialTick, int destroyStage) {
        bindTexture(RenderTileEntityColossalChest.TEXTURES.get(PropertyMaterial.Type.WOOD));
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
