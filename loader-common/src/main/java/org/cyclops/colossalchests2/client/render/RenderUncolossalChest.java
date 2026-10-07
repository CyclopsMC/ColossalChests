package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.cyclops.colossalchests2.blockentity.BlockEntityUncolossalChest;

import java.util.Calendar;

/**
 * Renders an uncolossal chest as a scaled down vanilla chest.
 * @author rubensworks
 */
public class RenderUncolossalChest implements BlockEntityRenderer<BlockEntityUncolossalChest> {

    private static final float SCALE = 0.3375F;
    /**
     * Items are drawn larger than placed chests, which would be barely visible in a slot.
     */
    private static final float ITEM_SCALE = 0.65F;

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final boolean christmas;

    public RenderUncolossalChest(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = model.getChild("bottom");
        this.lid = model.getChild("lid");
        this.lock = model.getChild("lock");
        Calendar calendar = Calendar.getInstance();
        this.christmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
    }

    @Override
    public void render(BlockEntityUncolossalChest chest, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        float openness = 1.0F - chest.getOpenNess(partialTick);
        openness = 1.0F - openness * openness * openness;
        float lidRotation = -(openness * ((float) Math.PI / 2F));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-chest.getFacing().toYRot()));
        float scale = chest.getLevel() == null ? ITEM_SCALE : SCALE;
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, 0, -0.5F);
        VertexConsumer buffer = Sheets.chooseMaterial(chest, ChestType.SINGLE, christmas).buffer(buffers, RenderType::entityCutout);
        lid.xRot = lidRotation;
        lock.xRot = lidRotation;
        lid.render(poseStack, buffer, packedLight, packedOverlay);
        lock.render(poseStack, buffer, packedLight, packedOverlay);
        bottom.render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

}
