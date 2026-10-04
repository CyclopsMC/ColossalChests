package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.colossalchests2.storage.DisplayStats;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.cyclopscore.helper.IModHelpers;
import org.joml.Matrix3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a Display wall as a recessed panel in a frame of the chest's material, with its item, count, fill level and
 * the upgrades affecting it, honouring the chest's visual settings.
 * @author rubensworks
 */
public class DisplayWallOverlay implements IChestOverlay {

    private static final ResourceLocation PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/display_panel");
    private static final ResourceLocation BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/display_bar");
    private static final ResourceLocation NEUTRAL_FRAME_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/chest_wall_iron");
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_BAR_BACKGROUND = 0xFF101010;
    private static final int COLOR_BAR = 0xFF40C040;
    private static final int COLOR_BAR_FULL = 0xFFD04030;
    private static final float PANEL_MIN = 1F / 16F;
    private static final float PANEL_MAX = 15F / 16F;
    private static final float BAR_X0 = 3F / 16F;
    private static final float BAR_X1 = 13F / 16F;
    private static final float BAR_Y0 = 2F / 16F;
    private static final float BAR_Y1 = 2.75F / 16F;
    private static final float TEXT_Y0 = 3.25F / 16F;
    private static final float TEXT_HEIGHT = 2.25F / 16F;
    private static final float ITEM_Y = 9.25F / 16F;
    private static final float ITEM_SIZE = 7.5F / 16F;
    private static final float INDICATOR_SIZE = 2.5F / 16F;
    private static final float LAYER = 0.0005F;

    /**
     * Temporary switches to compare looks in-game. 0: material frame, 1: neutral frame, 2: no frame.
     */
    public static int frameStyle = 0;
    /**
     * 0: flat inventory icon, 1: inventory icon shaded by the world, 2: item frame style.
     */
    public static int itemStyle = 1;

    @Override
    public void render(BlockEntityChestCore core, BlockPos pos, Direction face, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Level level = core.getLevel();
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        poseStack.pushPose();
        renderPanel(core, atlas, poseStack, buffers, light, overlay);
        if (level.getBlockEntity(pos) instanceof BlockEntityChestWall wall && !wall.getDisplayed().isEmpty()) {
            ChestSettings settings = core.getSettings();
            DisplayStats stats = wall.getDisplayStats();
            poseStack.translate(0, 0, LAYER);
            renderDisplayedItem(poseStack, buffers, level, wall.getDisplayed(), light);
            poseStack.translate(0, 0, LAYER);
            if (settings.showCounts()) {
                String count = IModHelpers.get().getGuiHelpers().quantityToScaledString(stats.count());
                ChestOverlayHelpers.renderText(poseStack, buffers, count, 0.5F, TEXT_Y0, TEXT_HEIGHT, COLOR_TEXT, light);
            }
            if (settings.showFillLevels()) {
                renderFillBar(atlas.getSprite(BAR_TEXTURE), stats.getFillLevel(), poseStack, buffers, light, overlay);
            }
            if (settings.showUpgradeIndicators()) {
                renderIndicators(atlas, stats, poseStack, buffers, light, overlay);
            }
        }
        poseStack.popPose();
    }

    private void renderPanel(BlockEntityChestCore core, TextureAtlas atlas, PoseStack poseStack, MultiBufferSource buffers,
                             int light, int overlay) {
        if (frameStyle != 2) {
            ResourceLocation frame = frameStyle == 0 && core.getBlockState().getBlock() instanceof BlockChestCore block
                    ? ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/chest_wall_" + block.getMaterial().getName())
                    : NEUTRAL_FRAME_TEXTURE;
            ChestOverlayHelpers.renderSprite(poseStack, buffers, atlas.getSprite(frame), 0, 0, 1, 1, light, overlay);
            poseStack.translate(0, 0, LAYER);
        }
        // The panel texture has a transparent rim, so the frame shows around it.
        float inset = frameStyle == 2 ? PANEL_MIN : 0;
        ChestOverlayHelpers.renderSprite(poseStack, buffers, atlas.getSprite(PANEL_TEXTURE), inset, inset, 1 - inset, 1 - inset, light, overlay);
    }

    private void renderDisplayedItem(PoseStack poseStack, MultiBufferSource buffers, Level level, ItemStack stack, int light) {
        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_Y, 0);
        if (itemStyle == 2) {
            // Like an item frame: blocks show their front face with some depth.
            poseStack.scale(ITEM_SIZE * 1.6F, ITEM_SIZE * 1.6F, ITEM_SIZE * 0.4F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
        } else {
            Matrix3f normal = new Matrix3f(poseStack.last().normal());
            // Flatten towards the face, so blocks look like their inventory icon without sticking out.
            poseStack.scale(ITEM_SIZE, ITEM_SIZE, 0.001F);
            if (itemStyle == 1) {
                // Keep the normals of the unflattened icon, so its faces are shaded like a block in the world.
                poseStack.last().normal().set(normal);
            }
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, light,
                    OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
        }
        poseStack.popPose();
    }

    private void renderFillBar(TextureAtlasSprite bar, float fill, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        ChestOverlayHelpers.renderSprite(poseStack, buffers, bar, BAR_X0, BAR_Y0, BAR_X1, BAR_Y1, light, overlay, COLOR_BAR_BACKGROUND);
        if (fill > 0) {
            poseStack.pushPose();
            poseStack.translate(0, 0, LAYER);
            ChestOverlayHelpers.renderSprite(poseStack, buffers, bar, BAR_X0, BAR_Y0, BAR_X0 + (BAR_X1 - BAR_X0) * fill, BAR_Y1,
                    light, overlay, fill >= 1 ? COLOR_BAR_FULL : COLOR_BAR);
            poseStack.popPose();
        }
    }

    private void renderIndicators(TextureAtlas atlas, DisplayStats stats, PoseStack poseStack, MultiBufferSource buffers,
                                  int light, int overlay) {
        // Flat icons of the upgrades that affect the shown item, in the top right corner of the panel.
        List<ChestUpgrade> indicators = new ArrayList<>();
        if (stats.locked()) {
            indicators.add(ChestUpgrades.LOCK);
        }
        if (stats.voided()) {
            indicators.add(ChestUpgrades.VOID);
        }
        if (stats.compressed()) {
            indicators.add(ChestUpgrades.COMPRESSION);
        }
        float x1 = PANEL_MAX - 0.75F / 16F;
        float y1 = PANEL_MAX - 0.75F / 16F;
        for (ChestUpgrade upgrade : indicators) {
            TextureAtlasSprite sprite = atlas.getSprite(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID,
                    "item/upgrade_" + upgrade.getId().getPath()));
            ChestOverlayHelpers.renderSprite(poseStack, buffers, sprite, x1 - INDICATOR_SIZE, y1 - INDICATOR_SIZE, x1, y1, light, overlay);
            x1 -= INDICATOR_SIZE;
        }
    }

}
