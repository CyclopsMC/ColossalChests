package org.cyclops.colossalchests.client.render.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityColossalChest extends RenderTileEntityChestBase<BlockEntityColossalChest, RenderTileEntityColossalChest.RenderState> {

    public static final Map<ChestMaterial, Identifier> TEXTURES_CHEST = Maps.newHashMap();
    public static final Map<ChestMaterial, Identifier> TEXTURES_INTERFACE = Maps.newHashMap();
    static {
        Calendar calendar = Calendar.getInstance();
        boolean christmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
        TEXTURES_CHEST.put(ChestMaterial.WOOD, Identifier.parse("entity/chest/" + (christmas ? "christmas" : "normal") + ""));
        TEXTURES_CHEST.put(ChestMaterial.COPPER, Identifier.parse("entity/chest/copper"));
        TEXTURES_CHEST.put(ChestMaterial.IRON, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "models/chest_iron"));
        TEXTURES_CHEST.put(ChestMaterial.SILVER, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "models/chest_silver"));
        TEXTURES_CHEST.put(ChestMaterial.GOLD, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "models/chest_gold"));
        TEXTURES_CHEST.put(ChestMaterial.DIAMOND, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "models/chest_diamond"));
        TEXTURES_CHEST.put(ChestMaterial.OBSIDIAN, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "models/chest_obsidian"));

        TEXTURES_INTERFACE.put(ChestMaterial.WOOD, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_wood"));
        TEXTURES_INTERFACE.put(ChestMaterial.COPPER, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_copper"));
        TEXTURES_INTERFACE.put(ChestMaterial.IRON, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_iron"));
        TEXTURES_INTERFACE.put(ChestMaterial.SILVER, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_silver"));
        TEXTURES_INTERFACE.put(ChestMaterial.GOLD, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_gold"));
        TEXTURES_INTERFACE.put(ChestMaterial.DIAMOND, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_diamond"));
        TEXTURES_INTERFACE.put(ChestMaterial.OBSIDIAN, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "blocks/interface_obsidian"));
    }

    public RenderTileEntityColossalChest(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void handleRotation(RenderState renderState, PoseStack poseStack) {
        // Move origin to center of chest
        if(renderState.structureComplete) {
            Vec3 renderOffset = renderState.renderOffset;
            poseStack.translate(-renderOffset.x, -renderOffset.y, -renderOffset.z);
        }

        // Rotate
        super.handleRotation(renderState, poseStack);

        // Move chest slightly higher
        poseStack.translate(0F, renderState.sizeSingular * 0.0625F, 0F);

        // Scale
        float size = renderState.sizeSingular * 1.125F;
        poseStack.scale(size, size, size);
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (renderState.structureComplete) {
            poseStack.pushPose();

            super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);

            // Render interface overlays
            if(renderState.openNessRaw == 0 && (GeneralConfig.alwaysShowInterfaceOverlay || Minecraft.getInstance().player.isCrouching())) {
                poseStack.pushPose();
                SpriteId materialInterface = getMaterialInterface(renderState);
                submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.text(materialInterface.atlasLocation()), (pose, vertexConsumer) -> {
                    for (Vec3i interfaceLocation : renderState.interfaceLocations) {
                        float translateX = (float) (interfaceLocation.getX() - cameraRenderState.pos.x());
                        float translateY = (float) (interfaceLocation.getY() - cameraRenderState.pos.y());
                        float translateZ = (float) (interfaceLocation.getZ() - cameraRenderState.pos.z());
                        poseStack.translate(translateX, translateY, translateZ);
                        submitInterface(poseStack, vertexConsumer, materials.get(materialInterface), interfaceLocation.equals(renderState.blockPos), renderState.lightCoords);
                        poseStack.translate(-translateX, -translateY, -translateZ);
                    }
                });
                poseStack.popPose();
            }
            poseStack.popPose();
        }
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntityColossalChest blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.material = blockEntity.getMaterial();
        renderState.direction = blockEntity.getRotation().getOpposite();
        renderState.structureComplete = blockEntity.isStructureComplete();
        renderState.renderOffset = blockEntity.getRenderOffset();
        renderState.sizeSingular = blockEntity.getSizeSingular();
        renderState.interfaceLocations = blockEntity.getInterfaceLocations();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    protected Direction getDirection(RenderState renderState) {
        return renderState.direction;
    }

    @Override
    protected SpriteId getMaterial(RenderState renderState) {
        return new SpriteId(Sheets.CHEST_SHEET, TEXTURES_CHEST.get(renderState.material));
    }

    protected SpriteId getMaterialInterface(RenderState renderState) {
        return new SpriteId(TextureAtlas.LOCATION_BLOCKS, TEXTURES_INTERFACE.get(renderState.material));
    }

    protected void setMatrixOrientation(PoseStack matrixStack, Direction direction) {
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
        matrixStack.mulPose(Axis.YP.rotationDegrees(rotationY));
        matrixStack.mulPose(Axis.XP.rotationDegrees(rotationX));
    }

    protected void submitInterface(PoseStack matrixStack, VertexConsumer buffer, TextureAtlasSprite sprite, boolean core, int combinedLightIn) {
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
            buffer.addVertex(matrix, posMax, posMax, indent).setColor(255, 255, 255, alpha).setUv(uMin, vMax).setLight(combinedLightIn);
            buffer.addVertex(matrix, posMax, posMin, indent).setColor(255, 255, 255, alpha).setUv(uMin, vMin).setLight(combinedLightIn);
            buffer.addVertex(matrix, posMin, posMin, indent).setColor(255, 255, 255, alpha).setUv(uMax, vMin).setLight(combinedLightIn);
            buffer.addVertex(matrix, posMin, posMax, indent).setColor(255, 255, 255, alpha).setUv(uMax, vMax).setLight(combinedLightIn);
            matrixStack.popPose();
        }
    }

    public static class RenderState extends RenderTileEntityChestBase.RenderState {
        public ChestMaterial material;
        public Direction direction;
        public boolean structureComplete;
        public Vec3 renderOffset;
        public int sizeSingular;
        public List<Vec3i> interfaceLocations;
    }
}
