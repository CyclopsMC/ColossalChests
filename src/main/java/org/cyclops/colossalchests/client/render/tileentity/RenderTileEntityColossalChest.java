package org.cyclops.colossalchests.client.render.tileentity;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.block.PropertyMaterial;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.cyclopscore.client.gui.image.Images;
import org.cyclops.cyclopscore.client.render.tileentity.RenderTileEntityModel;
import org.cyclops.cyclopscore.init.ModBase;
import org.lwjgl.opengl.GL11;

import java.util.Calendar;
import java.util.Map;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityColossalChest extends RenderTileEntityModel<TileColossalChest, ModelChest> {

    public static final Map<PropertyMaterial.Type, ResourceLocation> TEXTURES_CHEST = Maps.newHashMap();
    public static final Map<PropertyMaterial.Type, ResourceLocation> TEXTURES_INTERFACE = Maps.newHashMap();
    static {
        Calendar calendar = Calendar.getInstance();
        boolean christmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
        TEXTURES_CHEST.put(PropertyMaterial.Type.WOOD, new ResourceLocation("textures/entity/chest/" + (christmas ? "christmas" : "normal") + ".png"));
        TEXTURES_CHEST.put(PropertyMaterial.Type.COPPER, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_copper.png"));
        TEXTURES_CHEST.put(PropertyMaterial.Type.IRON, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_iron.png"));
        TEXTURES_CHEST.put(PropertyMaterial.Type.SILVER, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_silver.png"));
        TEXTURES_CHEST.put(PropertyMaterial.Type.GOLD, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_gold.png"));
        TEXTURES_CHEST.put(PropertyMaterial.Type.DIAMOND, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_diamond.png"));

        TEXTURES_INTERFACE.put(PropertyMaterial.Type.WOOD, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_wood.png"));
        TEXTURES_INTERFACE.put(PropertyMaterial.Type.COPPER, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_copper.png"));
        TEXTURES_INTERFACE.put(PropertyMaterial.Type.IRON, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_iron.png"));
        TEXTURES_INTERFACE.put(PropertyMaterial.Type.SILVER, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_silver.png"));
        TEXTURES_INTERFACE.put(PropertyMaterial.Type.GOLD, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_gold.png"));
        TEXTURES_INTERFACE.put(PropertyMaterial.Type.DIAMOND, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_diamond.png"));
    }

	/**
     * Make a new instance.
     * @param model The model to render.
     */
    public RenderTileEntityColossalChest(ModelChest model) {
        super(model, null);
    }

    @Override
    protected void preRotate(TileColossalChest chestTile) {
        if(chestTile.isStructureComplete()) {
            Vec3d renderOffset = chestTile.getRenderOffset();
            GlStateManager.translate(-renderOffset.xCoord, renderOffset.yCoord, renderOffset.zCoord);
        }
        GlStateManager.translate(0.5F, 0.5F - chestTile.getSizeSingular() * 0.0625F, 0.5F);
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
            bindTexture(TEXTURES_CHEST.get(chestTile.getMaterial()));
            GlStateManager.pushMatrix();
            if (ColossalChestConfig.chestAnimation) {
                float lidangle = chestTile.prevLidAngle + (chestTile.lidAngle - chestTile.prevLidAngle) * partialTick;
                lidangle = 1.0F - lidangle;
                lidangle = 1.0F - lidangle * lidangle * lidangle;
                model.chestLid.rotateAngleX = -(lidangle * (float) Math.PI / 2.0F);
            }
            GlStateManager.translate(0, -0.0625F * 8, 0);
            model.renderAll();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void renderTileEntityAt(TileColossalChest tile, double x, double y, double z, float partialTick, int destroyStage) {
        super.renderTileEntityAt(tile, x, y, z, partialTick, destroyStage);
        if(tile.isStructureComplete() && tile.lidAngle == 0 && (GeneralConfig.alwaysShowInterfaceOverlay || Minecraft.getMinecraft().player.isSneaking())) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.disableLighting();

            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.translate((float) x, (float) y, (float) z);
            bindTexture(TEXTURES_INTERFACE.get(tile.getMaterial()));
            for (Vec3i interfaceLocation : tile.getInterfaceLocations()) {
                float translateX = interfaceLocation.getX() - tile.getPos().getX();
                float translateY = interfaceLocation.getY() - tile.getPos().getY();
                float translateZ = interfaceLocation.getZ() - tile.getPos().getZ();
                GlStateManager.translate(translateX, translateY, translateZ);
                renderInterface(interfaceLocation.equals(tile.getPos()));
                GlStateManager.translate(-translateX, -translateY, -translateZ);
            }

            GlStateManager.enableLighting();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileColossalChest tile) {
        return true;
    }

    /**
     * Sets the OpenGL matrix orientation for the given direction.
     * @param direction The direction to orient the OpenGL matrix to.
     */
    protected void setMatrixOrientation(EnumFacing direction) {
        float translateX = -1F - direction.getFrontOffsetX();
        float translateY = direction.getFrontOffsetY();
        float translateZ = direction.getFrontOffsetZ();
        if (direction == EnumFacing.NORTH) {
            translateZ += 1F;
            translateX += 2F;
            translateY -= 1F;
        } else if (direction == EnumFacing.EAST) {
            translateX += 3F;
            translateY -= 1F;
            translateZ += 1F;
        } else if (direction == EnumFacing.WEST) {
            translateY -= 1F;
        } else if (direction == EnumFacing.SOUTH) {
            translateX += 1F;
            translateY -= 1F;
        } else if (direction == EnumFacing.UP) {
            translateX += 1F;
            translateZ += 1F;
        } else if (direction == EnumFacing.DOWN) {
            translateX += 1F;
        }
        GlStateManager.translate(translateX * 16, translateY * 16, translateZ * 16);

        short rotationY = 0;
        short rotationX = 0;
        if (direction == EnumFacing.SOUTH) {
            rotationY = 0;
        } else if (direction == EnumFacing.NORTH) {
            rotationY = 180;
        } else if (direction == EnumFacing.EAST) {
            rotationY = 90;
        } else if (direction == EnumFacing.WEST) {
            rotationY = -90;
        } else if (direction == EnumFacing.UP) {
            rotationX = -90;
        } else if (direction == EnumFacing.DOWN) {
            rotationX = 90;
        }
        GlStateManager.rotate((float) rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) rotationX, 1.0F, 0.0F, 0.0F);
    }

    protected void renderInterface(boolean core) {
        for (EnumFacing side : EnumFacing.VALUES) {
            GlStateManager.pushMatrix();
            float scale = 0.063F;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.scale(1, -1, 1);

            setMatrixOrientation(side);
            VertexBuffer worldRenderer = Tessellator.getInstance().getBuffer();
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            float indent = -0.2F;
            if (side == EnumFacing.UP) indent = 0F;
            if (side == EnumFacing.DOWN) indent *= 2;
            int alpha = core ? 255 : 150;
            float posMin = core ? 5F : 6F;
            float posMax = 16F - posMin;
            float texMin = 1F / 16F * posMin;
            float texMax = 1F - texMin;
            worldRenderer.pos(posMax, posMax, indent).tex(texMax, texMax).color(255, 255, 255, alpha).endVertex();
            worldRenderer.pos(posMax, posMin, indent).tex(texMax, texMin).color(255, 255, 255, alpha).endVertex();
            worldRenderer.pos(posMin, posMin, indent).tex(texMin, texMin).color(255, 255, 255, alpha).endVertex();
            worldRenderer.pos(posMin, posMax, indent).tex(texMin, texMax).color(255, 255, 255, alpha).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();
        }
    }
}
