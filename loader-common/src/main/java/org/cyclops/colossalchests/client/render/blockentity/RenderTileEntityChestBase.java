package org.cyclops.colossalchests.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * A modified copy of {@link ChestRenderer}.
 * @author rubensworks
 */
public abstract class RenderTileEntityChestBase<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {

    private final ChestModel singleModel;
    private final boolean xmasTextures = ChestRenderer.xmasTextures();

    public RenderTileEntityChestBase(BlockEntityRendererProvider.Context context) {
        this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
    }

    protected abstract Direction getDirection(T tileEntityIn);

    protected Material getMaterial(T tileEntity) {
        return Sheets.chooseMaterial(tileEntity, ChestType.SINGLE, this.xmasTextures);
    }

    protected void handleRotation(T blockEntity, PoseStack poseStack) {
        float f = getDirection(blockEntity).toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-f));
    }

    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        handleRotation(blockEntity, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        float g = blockEntity.getOpenNess(partialTick);
        g = 1.0F - g;
        g = 1.0F - g * g * g;
        Material material = getMaterial(blockEntity);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, RenderType::entityCutout);
        this.render(poseStack, vertexConsumer, this.singleModel, g, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void render(PoseStack poseStack, VertexConsumer buffer, ChestModel model, float openness, int packedLight, int packedOverlay) {
        model.setupAnim(openness);
        model.renderToBuffer(poseStack, buffer, packedLight, packedOverlay);
    }

}
