package org.cyclops.colossalchests.client.render.tileentity;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3i;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityModel;
import org.lwjgl.opengl.GL11;

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
            Vec3i renderOffset = chestTile.getRenderOffset();
            GlStateManager.translate(-renderOffset.getX(), renderOffset.getY(), renderOffset.getZ());
        }
        GlStateManager.translate(0.5F, 0, 0.5F);
        GlStateManager.scale(3, 3, 3);
    }

    @Override
    protected void postRotate(TileColossalChest tile) {
        GL11.glTranslatef(-0.5F, 0, -0.5F);
    }

    @Override
    protected void renderModel(TileColossalChest chestTile, ModelChest model, float partialTick, int destroyStage) {
        if(chestTile.isStructureComplete()) {
            float lidangle = chestTile.prevLidAngle + (chestTile.lidAngle - chestTile.prevLidAngle) * partialTick;
            lidangle = 1.0F - lidangle;
            lidangle = 1.0F - lidangle * lidangle * lidangle;
            model.chestLid.rotateAngleX = -(lidangle * (float) Math.PI / 2.0F);
            GlStateManager.translate(0, -0.3333F, 0);
            model.renderAll();
            GlStateManager.scale(1 / 3, 1 / 3, 1 / 3);
        }
    }
}
