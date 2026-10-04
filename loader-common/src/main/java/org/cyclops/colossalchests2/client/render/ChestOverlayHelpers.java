package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Drawing helpers for {@link IChestOverlay}s, in the face's unit square.
 * @author rubensworks
 */
public final class ChestOverlayHelpers {

    private ChestOverlayHelpers() {
    }

    /**
     * Draw a whole block atlas sprite, stretched over an area.
     * @param poseStack The pose stack.
     * @param buffers The buffers.
     * @param sprite A sprite on the block atlas.
     * @param x0 Left edge.
     * @param y0 Bottom edge.
     * @param x1 Right edge.
     * @param y1 Top edge.
     * @param light The packed light.
     * @param overlay The packed overlay.
     */
    public static void renderSprite(PoseStack poseStack, MultiBufferSource buffers, TextureAtlasSprite sprite,
                                    float x0, float y0, float x1, float y1, int light, int overlay) {
        renderSprite(poseStack, buffers, sprite, x0, y0, x1, y1, light, overlay, 0xFFFFFFFF);
    }

    /**
     * {@link #renderSprite(PoseStack, MultiBufferSource, TextureAtlasSprite, float, float, float, float, int, int)},
     * tinted.
     * @param color The ARGB tint.
     */
    public static void renderSprite(PoseStack poseStack, MultiBufferSource buffers, TextureAtlasSprite sprite,
                                    float x0, float y0, float x1, float y1, int light, int overlay, int color) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        PoseStack.Pose pose = poseStack.last();
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        // Texture v grows downwards, face y grows upwards.
        float v0 = sprite.getV1();
        float v1 = sprite.getV0();
        vertex(buffer, pose, x0, y0, u0, v0, light, overlay, color);
        vertex(buffer, pose, x1, y0, u1, v0, light, overlay, color);
        vertex(buffer, pose, x1, y1, u1, v1, light, overlay, color);
        vertex(buffer, pose, x0, y1, u0, v1, light, overlay, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int light, int overlay, int color) {
        buffer.addVertex(pose, x, y, 0)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    /**
     * Draw an item flat on the face, like in a GUI slot.
     * @param poseStack The pose stack.
     * @param buffers The buffers.
     * @param level The level.
     * @param stack The item.
     * @param centerX Horizontal center.
     * @param centerY Vertical center.
     * @param size Width and height.
     * @param light The packed light.
     */
    public static void renderItem(PoseStack poseStack, MultiBufferSource buffers, Level level, ItemStack stack,
                                  float centerX, float centerY, float size, int light) {
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        // Flatten towards the face, so 3D block items do not stick out of the chest.
        poseStack.scale(size, size, 0.001F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
        poseStack.popPose();
    }

    /**
     * @return If the player sneaks, which reveals the hidden members of a chest.
     */
    public static boolean isRevealingMembers() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCrouching();
    }

    /**
     * Draw a line of text on the face, centered horizontally.
     * @param poseStack The pose stack.
     * @param buffers The buffers.
     * @param text The text.
     * @param centerX Horizontal center.
     * @param y0 Bottom edge.
     * @param height Height of the text.
     * @param color The RGB color.
     * @param light The packed light.
     */
    public static void renderText(PoseStack poseStack, MultiBufferSource buffers, String text, float centerX, float y0, float height,
                                  int color, int light) {
        Font font = Minecraft.getInstance().font;
        float scale = height / font.lineHeight;
        poseStack.pushPose();
        // Font y grows downwards, face y upwards.
        poseStack.translate(centerX, y0 + height, 0);
        poseStack.scale(scale, -scale, scale);
        font.drawInBatch(text, -font.width(text) / 2F, 0, color, false, poseStack.last().pose(), buffers,
                Font.DisplayMode.POLYGON_OFFSET, 0, light);
        poseStack.popPose();
    }

}
