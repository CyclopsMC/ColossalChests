package org.cyclops.colossalchests.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.jetbrains.annotations.Nullable;

/**
 * Renderer for the {@link org.cyclops.colossalchests.block.ColossalChest}.
 * @author rubensworks
 *
 */
public class RenderTileEntityUncolossalChest extends RenderTileEntityChestBase<BlockEntityUncolossalChest, RenderTileEntityUncolossalChest.RenderState> {

    public RenderTileEntityUncolossalChest(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(BlockEntityUncolossalChest blockEntity, RenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.rotation = blockEntity.getRotation();
    }

    @Override
    public boolean shouldRender(BlockEntityUncolossalChest blockEntity, Vec3 cameraPos) {
        return blockEntity.getBlockPos() == BlockPos.ZERO || super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    protected Direction getDirection(RenderState renderState) {
        if (renderState.blockPos == BlockPos.ZERO) {
            return Direction.SOUTH;
        }
        return renderState.rotation;
    }

    @Override
    public void submit(RenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.325F, 0F, 0.325F);
        float size = 0.3F * 1.125F;
        poseStack.scale(size, size, size);
        super.submit(renderState, poseStack, submitNodeCollector, cameraRenderState);
        poseStack.popPose();
    }

    public static class RenderState extends RenderTileEntityChestBase.RenderState {
        public Direction rotation;
    }

}
