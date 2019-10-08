package org.cyclops.colossalchests.client.render.tileentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.block.ColossalChestConfig;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
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
public class RenderTileEntityColossalChest extends RenderTileEntityModel<TileColossalChest, ChestModel> {

    public static final Map<ChestMaterial, ResourceLocation> TEXTURES_CHEST = Maps.newHashMap();
    public static final Map<ChestMaterial, ResourceLocation> TEXTURES_INTERFACE = Maps.newHashMap();
    static {
        Calendar calendar = Calendar.getInstance();
        boolean christmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
        TEXTURES_CHEST.put(ChestMaterial.WOOD, new ResourceLocation("textures/entity/chest/" + (christmas ? "christmas" : "normal") + ".png"));
        TEXTURES_CHEST.put(ChestMaterial.COPPER, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_copper.png"));
        TEXTURES_CHEST.put(ChestMaterial.IRON, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_iron.png"));
        TEXTURES_CHEST.put(ChestMaterial.SILVER, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_silver.png"));
        TEXTURES_CHEST.put(ChestMaterial.GOLD, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_gold.png"));
        TEXTURES_CHEST.put(ChestMaterial.DIAMOND, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_diamond.png"));
        TEXTURES_CHEST.put(ChestMaterial.OBSIDIAN, new ResourceLocation(Reference.MOD_ID, ColossalChests._instance.getReferenceValue(ModBase.REFKEY_TEXTURE_PATH_MODELS) + "chest_obsidian.png"));

        TEXTURES_INTERFACE.put(ChestMaterial.WOOD, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_wood.png"));
        TEXTURES_INTERFACE.put(ChestMaterial.COPPER, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_copper.png"));
        TEXTURES_INTERFACE.put(ChestMaterial.IRON, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_iron.png"));
        TEXTURES_INTERFACE.put(ChestMaterial.SILVER, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_silver.png"));
        TEXTURES_INTERFACE.put(ChestMaterial.GOLD, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_gold.png"));
        TEXTURES_INTERFACE.put(ChestMaterial.DIAMOND, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_diamond.png"));
        TEXTURES_INTERFACE.put(ChestMaterial.OBSIDIAN, new ResourceLocation(Reference.MOD_ID, "textures/blocks/interface_obsidian.png"));
    }

	/**
     * Make a new instance.
     * @param model The model to render.
     */
    public RenderTileEntityColossalChest(ChestModel model) {
        super(model, null);
    }

    @Override
    protected void preRotate(TileColossalChest chestTile) {
        if(chestTile.isStructureComplete()) {
            Vec3d renderOffset = chestTile.getRenderOffset();
            GlStateManager.translated(-renderOffset.x, renderOffset.y, renderOffset.z);
        }
        GlStateManager.translatef(0.5F, 0.5F - chestTile.getSizeSingular() * 0.0625F, 0.5F);
        float size = chestTile.getSizeSingular() * 1.125F;
        GlStateManager.scalef(size, size, size);
    }

    @Override
    protected void postRotate(TileColossalChest tile) {
        GlStateManager.translatef(-0.5F, 0, -0.5F);
    }

    @Override
    protected void renderModel(TileColossalChest chestTile, ChestModel model, float partialTick, int destroyStage) {
        if(chestTile.isStructureComplete()) {
            bindTexture(TEXTURES_CHEST.get(chestTile.getMaterial()));
            GlStateManager.pushMatrix();
            if (ColossalChestConfig.chestAnimation) {
                float lidangle = chestTile.prevLidAngle + (chestTile.lidAngle - chestTile.prevLidAngle) * partialTick;
                lidangle = 1.0F - lidangle;
                lidangle = 1.0F - lidangle * lidangle * lidangle;
                model.getLid().rotateAngleX = -(lidangle * (float) Math.PI / 2.0F);
            }
            GlStateManager.translatef(0, -0.0625F * 8, 0);
            model.renderAll();
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void render(TileColossalChest tile, double x, double y, double z, float partialTick, int destroyStage) {
        super.render(tile, x, y, z, partialTick, destroyStage);
        if(tile.isStructureComplete() && tile.lidAngle == 0 && (GeneralConfig.alwaysShowInterfaceOverlay || Minecraft.getInstance().player.isSneaking())) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.pushLightingAttributes();
            GlStateManager.disableLighting();

            GlStateManager.enableRescaleNormal();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.translatef((float) x, (float) y, (float) z);
            bindTexture(TEXTURES_INTERFACE.get(tile.getMaterial()));
            for (Vec3i interfaceLocation : tile.getInterfaceLocations()) {
                float translateX = interfaceLocation.getX() - tile.getPos().getX();
                float translateY = interfaceLocation.getY() - tile.getPos().getY();
                float translateZ = interfaceLocation.getZ() - tile.getPos().getZ();
                GlStateManager.translatef(translateX, translateY, translateZ);
                renderInterface(interfaceLocation.equals(tile.getPos()));
                GlStateManager.translatef(-translateX, -translateY, -translateZ);
            }

            GlStateManager.enableLighting();
            GlStateManager.popAttributes();
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
    protected void setMatrixOrientation(Direction direction) {
        float translateX = -1F - direction.getXOffset();
        float translateY = direction.getYOffset();
        float translateZ = direction.getZOffset();
        if (direction == Direction.NORTH) {
            translateZ += 1F;
            translateX += 2F;
            translateY -= 1F;
        } else if (direction == Direction.EAST) {
            translateX += 3F;
            translateY -= 1F;
            translateZ += 1F;
        } else if (direction == Direction.WEST) {
            translateY -= 1F;
        } else if (direction == Direction.SOUTH) {
            translateX += 1F;
            translateY -= 1F;
        } else if (direction == Direction.UP) {
            translateX += 1F;
            translateZ += 1F;
        } else if (direction == Direction.DOWN) {
            translateX += 1F;
        }
        GlStateManager.translatef(translateX * 16, translateY * 16, translateZ * 16);

        short rotationY = 0;
        short rotationX = 0;
        if (direction == Direction.SOUTH) {
            rotationY = 0;
        } else if (direction == Direction.NORTH) {
            rotationY = 180;
        } else if (direction == Direction.EAST) {
            rotationY = 90;
        } else if (direction == Direction.WEST) {
            rotationY = -90;
        } else if (direction == Direction.UP) {
            rotationX = -90;
        } else if (direction == Direction.DOWN) {
            rotationX = 90;
        }
        GlStateManager.rotatef((float) rotationY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef((float) rotationX, 1.0F, 0.0F, 0.0F);
    }

    protected void renderInterface(boolean core) {
        for (Direction side : Direction.values()) {
            GlStateManager.pushMatrix();
            float scale = 0.063F;
            GlStateManager.scalef(scale, scale, scale);
            GlStateManager.scalef(1, -1, 1);

            setMatrixOrientation(side);
            BufferBuilder worldRenderer = Tessellator.getInstance().getBuffer();
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            float indent = -0.2F;
            if (side == Direction.UP) indent = -15.8F;
            if (side == Direction.DOWN) indent *= 2;
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
