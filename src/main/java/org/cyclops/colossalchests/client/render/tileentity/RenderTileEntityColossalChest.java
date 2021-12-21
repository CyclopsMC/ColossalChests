package org.cyclops.colossalchests.client.render.tileentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.tileentity.TileColossalChest;

import java.util.Calendar;
import java.util.Map;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
@OnlyIn(Dist.CLIENT)
public class RenderTileEntityColossalChest extends RenderTileEntityChestBase<TileColossalChest> {

    public static final Map<ChestMaterial, ResourceLocation> TEXTURES_CHEST = Maps.newHashMap();
    public static final Map<ChestMaterial, ResourceLocation> TEXTURES_INTERFACE = Maps.newHashMap();
    static {
        Calendar calendar = Calendar.getInstance();
        boolean christmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
        TEXTURES_CHEST.put(ChestMaterial.WOOD, new ResourceLocation("entity/chest/" + (christmas ? "christmas" : "normal") + ""));
        TEXTURES_CHEST.put(ChestMaterial.COPPER, new ResourceLocation(Reference.MOD_ID, "models/chest_copper"));
        TEXTURES_CHEST.put(ChestMaterial.IRON, new ResourceLocation(Reference.MOD_ID, "models/chest_iron"));
        TEXTURES_CHEST.put(ChestMaterial.SILVER, new ResourceLocation(Reference.MOD_ID, "models/chest_silver"));
        TEXTURES_CHEST.put(ChestMaterial.GOLD, new ResourceLocation(Reference.MOD_ID, "models/chest_gold"));
        TEXTURES_CHEST.put(ChestMaterial.DIAMOND, new ResourceLocation(Reference.MOD_ID, "models/chest_diamond"));
        TEXTURES_CHEST.put(ChestMaterial.OBSIDIAN, new ResourceLocation(Reference.MOD_ID, "models/chest_obsidian"));

        TEXTURES_INTERFACE.put(ChestMaterial.WOOD, new ResourceLocation(Reference.MOD_ID, "blocks/interface_wood"));
        TEXTURES_INTERFACE.put(ChestMaterial.COPPER, new ResourceLocation(Reference.MOD_ID, "blocks/interface_copper"));
        TEXTURES_INTERFACE.put(ChestMaterial.IRON, new ResourceLocation(Reference.MOD_ID, "blocks/interface_iron"));
        TEXTURES_INTERFACE.put(ChestMaterial.SILVER, new ResourceLocation(Reference.MOD_ID, "blocks/interface_silver"));
        TEXTURES_INTERFACE.put(ChestMaterial.GOLD, new ResourceLocation(Reference.MOD_ID, "blocks/interface_gold"));
        TEXTURES_INTERFACE.put(ChestMaterial.DIAMOND, new ResourceLocation(Reference.MOD_ID, "blocks/interface_diamond"));
        TEXTURES_INTERFACE.put(ChestMaterial.OBSIDIAN, new ResourceLocation(Reference.MOD_ID, "blocks/interface_obsidian"));
    }
    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.getMap().location().equals(Atlases.CHEST_SHEET)) {
            for (ResourceLocation value : TEXTURES_CHEST.values()) {
                event.addSprite(value);
            }
            for (ResourceLocation value : TEXTURES_INTERFACE.values()) {
                event.addSprite(value);
            }
        }
    }

    public RenderTileEntityColossalChest(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
        super(tileEntityRendererDispatcher);
    }

    @Override
    protected void handleRotation(TileColossalChest tile, MatrixStack matrixStack) {
        // Move origin to center of chest
        if(tile.isStructureComplete()) {
            Vector3d renderOffset = tile.getRenderOffset();
            matrixStack.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
        }

        // Rotate
        super.handleRotation(tile, matrixStack);

        // Move chest slightly higher
        matrixStack.translate(0F, tile.getSizeSingular() * 0.0625F, 0F);

        // Scale
        float size = tile.getSizeSingular() * 1.125F;
        matrixStack.scale(size, size, size);
    }

    @Override
    public void render(TileColossalChest tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        if (tile.isStructureComplete()) {
            matrixStack.pushPose();

            super.render(tile, partialTicks, matrixStack, renderTypeBuffer, combinedLightIn, combinedOverlayIn);

            // Render interface overlays
            if(tile.isStructureComplete() && tile.lidAngle == 0 && (GeneralConfig.alwaysShowInterfaceOverlay || Minecraft.getInstance().player.isCrouching())) {
                matrixStack.pushPose();
                RenderMaterial materialInterface = getMaterialInterface(tile);
                IVertexBuilder buffer = materialInterface.buffer(renderTypeBuffer, RenderType::text);
                for (Vector3i interfaceLocation : tile.getInterfaceLocations()) {
                    float translateX = interfaceLocation.getX() - tile.getBlockPos().getX();
                    float translateY = interfaceLocation.getY() - tile.getBlockPos().getY();
                    float translateZ = interfaceLocation.getZ() - tile.getBlockPos().getZ();
                    matrixStack.translate(translateX, translateY, translateZ);
                    renderInterface(matrixStack, buffer, materialInterface.sprite(), interfaceLocation.equals(tile.getBlockPos()), combinedLightIn);
                    matrixStack.translate(-translateX, -translateY, -translateZ);
                }
                matrixStack.popPose();
            }
            matrixStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileColossalChest tile) {
        return true;
    }

    @Override
    protected Direction getDirection(TileColossalChest tile) {
        return tile.getRotation().getOpposite();
    }

    @Override
    protected RenderMaterial getMaterial(TileColossalChest tile) {
        return new RenderMaterial(Atlases.CHEST_SHEET, TEXTURES_CHEST.get(tile.getMaterial()));
    }

    protected RenderMaterial getMaterialInterface(TileColossalChest tile) {
        return new RenderMaterial(Atlases.CHEST_SHEET, TEXTURES_INTERFACE.get(tile.getMaterial()));
    }

    protected void setMatrixOrientation(MatrixStack matrixStack, Direction direction) {
        float translateX = -1F - direction.getStepX();
        float translateY = direction.getStepY();
        float translateZ = direction.getStepZ();
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
        matrixStack.translate(translateX * 16, translateY * 16, translateZ * 16);

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
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(rotationY));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(rotationX));
    }

    protected void renderInterface(MatrixStack matrixStack, IVertexBuilder buffer, TextureAtlasSprite sprite, boolean core, int combinedLightIn) {
        for (Direction side : Direction.values()) {
            matrixStack.pushPose();
            float scale = 0.063F;
            matrixStack.scale(scale, scale, scale);
            matrixStack.scale(1, -1, 1);

            setMatrixOrientation(matrixStack, side);
            float indent = -0.2F;
            if (side == Direction.UP) indent = -15.8F;
            if (side == Direction.DOWN) indent *= 2;
            int alpha = 255;
            float posMin = core ? 5F : 6F;
            float posMax = 16F - posMin;

            float uvScale = posMin / 16F;
            float uMin = (sprite.getU1() - sprite.getU0()) * uvScale + sprite.getU0();
            float uMax = (sprite.getU1() - sprite.getU0()) * (1 - uvScale) + sprite.getU0();
            float vMin = (sprite.getV1() - sprite.getV0()) * uvScale + sprite.getV0();
            float vMax = (sprite.getV1() - sprite.getV0()) * (1 - uvScale) + sprite.getV0();

            Matrix4f matrix = matrixStack.last().pose();
            buffer.vertex(matrix, posMax, posMax, indent).color(255, 255, 255, alpha).uv(uMin, vMax).uv2(combinedLightIn).endVertex();
            buffer.vertex(matrix, posMax, posMin, indent).color(255, 255, 255, alpha).uv(uMin, vMin).uv2(combinedLightIn).endVertex();
            buffer.vertex(matrix, posMin, posMin, indent).color(255, 255, 255, alpha).uv(uMax, vMin).uv2(combinedLightIn).endVertex();
            buffer.vertex(matrix, posMin, posMax, indent).color(255, 255, 255, alpha).uv(uMax, vMax).uv2(combinedLightIn).endVertex();
            matrixStack.popPose();
        }
    }
}
