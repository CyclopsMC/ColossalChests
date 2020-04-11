package org.cyclops.colossalchests.client.render.tileentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
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
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
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
        if (event.getMap().getTextureLocation().equals(Atlases.CHEST_ATLAS)) {
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
            Vec3d renderOffset = tile.getRenderOffset();
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
            matrixStack.push();

            super.render(tile, partialTicks, matrixStack, renderTypeBuffer, combinedLightIn, combinedOverlayIn);

            // Render interface overlays
            if(tile.isStructureComplete() && tile.lidAngle == 0 && (GeneralConfig.alwaysShowInterfaceOverlay || Minecraft.getInstance().player.isCrouching())) {
                matrixStack.push();
                Material materialInterface = getMaterialInterface(tile);
                IVertexBuilder buffer = materialInterface.getBuffer(renderTypeBuffer, RenderType::getText);
                for (Vec3i interfaceLocation : tile.getInterfaceLocations()) {
                    float translateX = interfaceLocation.getX() - tile.getPos().getX();
                    float translateY = interfaceLocation.getY() - tile.getPos().getY();
                    float translateZ = interfaceLocation.getZ() - tile.getPos().getZ();
                    matrixStack.translate(translateX, translateY, translateZ);
                    renderInterface(matrixStack, buffer, materialInterface.getSprite(), interfaceLocation.equals(tile.getPos()), combinedLightIn);
                    matrixStack.translate(-translateX, -translateY, -translateZ);
                }
                matrixStack.pop();
            }
            matrixStack.pop();
        }
    }

    @Override
    public boolean isGlobalRenderer(TileColossalChest tile) {
        return true;
    }

    @Override
    protected Direction getDirection(TileColossalChest tile) {
        return tile.getRotation().getOpposite();
    }

    @Override
    protected Material getMaterial(TileColossalChest tile) {
        return new Material(Atlases.CHEST_ATLAS, TEXTURES_CHEST.get(tile.getMaterial()));
    }

    protected Material getMaterialInterface(TileColossalChest tile) {
        return new Material(Atlases.CHEST_ATLAS, TEXTURES_INTERFACE.get(tile.getMaterial()));
    }

    protected void setMatrixOrientation(MatrixStack matrixStack, Direction direction) {
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
        matrixStack.rotate(Vector3f.YP.rotationDegrees(rotationY));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(rotationX));
    }

    protected void renderInterface(MatrixStack matrixStack, IVertexBuilder buffer, TextureAtlasSprite sprite, boolean core, int combinedLightIn) {
        for (Direction side : Direction.values()) {
            matrixStack.push();
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
            float uMin = (sprite.getMaxU() - sprite.getMinU()) * uvScale + sprite.getMinU();
            float uMax = (sprite.getMaxU() - sprite.getMinU()) * (1 - uvScale) + sprite.getMinU();
            float vMin = (sprite.getMaxV() - sprite.getMinV()) * uvScale + sprite.getMinV();
            float vMax = (sprite.getMaxV() - sprite.getMinV()) * (1 - uvScale) + sprite.getMinV();

            Matrix4f matrix = matrixStack.getLast().getMatrix();
            buffer.pos(matrix, posMax, posMax, indent).color(255, 255, 255, alpha).tex(uMin, vMax).lightmap(combinedLightIn).endVertex();
            buffer.pos(matrix, posMax, posMin, indent).color(255, 255, 255, alpha).tex(uMin, vMin).lightmap(combinedLightIn).endVertex();
            buffer.pos(matrix, posMin, posMin, indent).color(255, 255, 255, alpha).tex(uMax, vMin).lightmap(combinedLightIn).endVertex();
            buffer.pos(matrix, posMin, posMax, indent).color(255, 255, 255, alpha).tex(uMax, vMax).lightmap(combinedLightIn).endVertex();
            matrixStack.pop();
        }
    }
}
