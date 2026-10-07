package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.cyclops.colossalchests2.api.ChestMaterial;
import org.cyclops.colossalchests2.api.client.ChestOverlays;
import org.cyclops.colossalchests2.api.client.IChestOverlay;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.multiblock.ChestShape;
import org.cyclops.colossalchests2.multiblock.ChestStructure;

import java.util.Calendar;

/**
 * Renders a formed chest as one giant vanilla-style chest whose body exactly fills the structure,
 * followed by the {@link IChestOverlay}s of its decorated members.
 * @author rubensworks
 */
public class RenderChestCore implements BlockEntityRenderer<BlockEntityChestCore> {

    /**
     * How far overlays float in front of the chest surface, far enough to avoid z-fighting.
     * The lock sticks out further, so it hides the part of an overlay right behind it, like any object in front.
     */
    public static final float OVERLAY_OFFSET = 1F / 32F;

    private static final float MODEL_HEIGHT = 14F / 16F;
    private static final float LID_PIVOT_Y = 9F / 16F;
    private static final float LID_PIVOT_Z = 1F / 16F;

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final boolean christmas;

    public RenderChestCore(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ModelLayers.CHEST);
        this.bottom = model.getChild("bottom");
        this.lid = model.getChild("lid");
        this.lock = model.getChild("lock");
        Calendar calendar = Calendar.getInstance();
        this.christmas = calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26;
    }

    @Override
    public void render(BlockEntityChestCore core, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ChestStructure structure = core.getStructure();
        Level level = core.getLevel();
        if (structure == null || level == null || !(core.getBlockState().getBlock() instanceof BlockChestCore block)) {
            return;
        }
        Direction facing = core.getFacing();
        float openness = core.getOpenness(partialTick);
        openness = 1.0F - openness;
        openness = 1.0F - openness * openness * openness;
        float lidRotation = -(openness * ((float) Math.PI / 2F));

        poseStack.pushPose();
        applyChestTransform(poseStack, core.getBlockPos(), structure, facing);
        int bodyLight = LevelRenderer.getLightColor(level, ChestShape.getFrontPos(structure, facing));
        VertexConsumer buffer = getMaterial(block.getMaterial()).buffer(buffers, RenderType::entityCutout);
        lid.xRot = lidRotation;
        lid.render(poseStack, buffer, bodyLight, packedOverlay);
        bottom.render(poseStack, buffer, bodyLight, packedOverlay);
        renderLock(poseStack, buffer, lidRotation, bodyLight, packedOverlay);
        poseStack.popPose();

        renderOverlays(core, structure, facing, lidRotation, partialTick, poseStack, buffers, packedOverlay);
    }

    /**
     * Map the vanilla chest model space onto the structure, relative to the core.
     * The model body spans 14 pixels in each direction, which becomes the structure size.
     */
    public static void applyChestTransform(PoseStack poseStack, BlockPos origin, ChestStructure structure, Direction facing) {
        float scale = structure.size() / MODEL_HEIGHT;
        poseStack.translate(
                structure.min().getX() - origin.getX() + structure.size() / 2F,
                structure.min().getY() - origin.getY(),
                structure.min().getZ() - origin.getZ() + structure.size() / 2F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5F, 0, -0.5F);
    }

    /**
     * Render the lock, which moves along with the lid like on a vanilla chest.
     */
    protected void renderLock(PoseStack poseStack, VertexConsumer buffer, float lidRotation, int light, int overlay) {
        lock.xRot = lidRotation;
        lock.render(poseStack, buffer, light, overlay);
    }

    protected void renderOverlays(BlockEntityChestCore core, ChestStructure structure, Direction facing, float lidRotation,
                                  float partialTick, PoseStack poseStack, MultiBufferSource buffers, int overlay) {
        Level level = core.getLevel();
        BlockPos origin = core.getBlockPos();
        for (BlockPos pos : core.getDecoratedPositions()) {
            Block block = level.getBlockState(pos).getBlock();
            IChestOverlay chestOverlay = block instanceof BlockChestCore ? CoreMarkerOverlay.INSTANCE : ChestOverlays.get(block);
            if (chestOverlay == null) {
                continue;
            }
            for (Direction face : ChestShape.getOuterFaces(structure, pos)) {
                poseStack.pushPose();
                if (lidRotation != 0 && ChestShape.isOnLid(structure, pos)) {
                    applyLidTransform(poseStack, origin, structure, facing, lidRotation);
                }
                poseStack.translate(pos.getX() - origin.getX(), pos.getY() - origin.getY(), pos.getZ() - origin.getZ());
                applyFaceTransform(poseStack, face, facing);
                int light = LevelRenderer.getLightColor(level, pos.relative(face));
                chestOverlay.render(core, pos, face, partialTick, poseStack, buffers, light, overlay);
                poseStack.popPose();
            }
        }
    }

    /**
     * Rotate around the lid hinge in core-relative space, so things on the lid move along with it.
     */
    public static void applyLidTransform(PoseStack poseStack, BlockPos origin, ChestStructure structure, Direction facing, float lidRotation) {
        float scale = structure.size() / MODEL_HEIGHT;
        applyChestTransform(poseStack, origin, structure, facing);
        poseStack.translate(0, LID_PIVOT_Y, LID_PIVOT_Z);
        poseStack.mulPose(Axis.XP.rotation(lidRotation));
        poseStack.translate(0, -LID_PIVOT_Y, -LID_PIVOT_Z);
        // Undo the chest transform.
        poseStack.translate(0.5F, 0, 0.5F);
        poseStack.scale(1 / scale, 1 / scale, 1 / scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(facing.toYRot()));
        poseStack.translate(
                -(structure.min().getX() - origin.getX() + structure.size() / 2F),
                -(structure.min().getY() - origin.getY()),
                -(structure.min().getZ() - origin.getZ() + structure.size() / 2F));
    }

    /**
     * Map a block's unit cube onto the face-local unit square of {@link IChestOverlay}.
     */
    public static void applyFaceTransform(PoseStack poseStack, Direction face, Direction facing) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        if (face.getAxis().isHorizontal()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-face.toYRot()));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(face == Direction.UP ? -90 : 90));
        }
        poseStack.translate(-0.5F, -0.5F, 0.5F + OVERLAY_OFFSET);
    }

    protected Material getMaterial(ChestMaterial material) {
        if (ChestMaterial.WOOD.equals(material)) {
            return christmas ? Sheets.CHEST_XMAS_LOCATION : Sheets.CHEST_LOCATION;
        }
        return new Material(Sheets.CHEST_SHEET, material.id().withPrefix("entity/chest/"));
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntityChestCore core) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

}
