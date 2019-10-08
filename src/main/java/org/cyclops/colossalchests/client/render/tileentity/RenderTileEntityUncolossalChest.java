package org.cyclops.colossalchests.client.render.tileentity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityModel;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityUncolossalChest extends RenderTileEntityModel<TileUncolossalChest, ChestModel> {

	/**
     * Make a new instance.
     * @param model The model to render.
     */
    public RenderTileEntityUncolossalChest(ChestModel model) {
        super(model, null);
    }

    @Override
    protected void preRotate(TileUncolossalChest chestTile) {
        GlStateManager.translatef(0.5F, 0.83F, 0.5F);
        float size = 0.3F * 1.125F;
        GlStateManager.scalef(size, size, size);
    }

    @Override
    protected void postRotate(TileUncolossalChest tile) {
        GlStateManager.translatef(-0.5F, 0, -0.5F);
    }

    @Override
    protected void renderModel(TileUncolossalChest chestTile, ChestModel model, float partialTick, int destroyStage) {
        bindTexture(RenderTileEntityColossalChest.TEXTURES_CHEST.get(ChestMaterial.WOOD));
        GlStateManager.pushMatrix();
        float lidangle = chestTile.prevLidAngle + (chestTile.lidAngle - chestTile.prevLidAngle) * partialTick;
        lidangle = 1.0F - lidangle;
        lidangle = 1.0F - lidangle * lidangle * lidangle;
        model.getLid().rotateAngleX = -(lidangle * (float) Math.PI / 2.0F);
        GlStateManager.translatef(0, -0.0625F * 8, 0);
        model.renderAll();
        GlStateManager.popMatrix();
    }
}
