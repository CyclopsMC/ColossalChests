package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.blockentity.DisplayOption;
import org.cyclops.colossalchests2.storage.DisplayStats;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.cyclopscore.helper.IModHelpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows each shown face of a Display wall as a recessed panel in a frame of the chest's material, with its item, count,
 * fill level and the upgrades affecting it, as the face's options say. Hidden faces show the plain chest, or the Display
 * wall icon while the player sneaks.
 * @author rubensworks
 */
public class DisplayWallOverlay implements IChestOverlay {

    private static final ResourceLocation PANEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/display_panel");
    private static final ResourceLocation HIDDEN_ICON = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/chest_wall_display_icon");
    private static final float ICON_MIN = 3F / 16F;
    private static final float ICON_MAX = 13F / 16F;
    private static final ResourceLocation BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/display_bar");
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_BAR_BACKGROUND = 0xFF101010;
    private static final int COLOR_BAR = 0xFF40C040;
    private static final int COLOR_BAR_FULL = 0xFFD04030;
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

    @Override
    public void render(BlockEntityChestCore core, BlockPos pos, Direction face, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Level level = core.getLevel();
        if (!(level.getBlockEntity(pos) instanceof BlockEntityChestWall wall)) {
            return;
        }
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        if (wall.isFaceHidden(face)) {
            // Like other functional walls, so its settings can be found again.
            if (ChestOverlayHelpers.isRevealingMembers()) {
                ChestOverlayHelpers.renderSprite(poseStack, buffers, atlas.getSprite(HIDDEN_ICON), ICON_MIN, ICON_MIN, ICON_MAX, ICON_MAX,
                        light, overlay);
            }
            return;
        }
        poseStack.pushPose();
        renderPanel(core, atlas, poseStack, buffers, light, overlay);
        ItemStack displayed = wall.getDisplayed(face);
        if (!displayed.isEmpty()) {
            DisplayStats stats = wall.getDisplayStats(face);
            poseStack.translate(0, 0, LAYER);
            renderDisplayedItem(poseStack, buffers, level, displayed, light);
            poseStack.translate(0, 0, LAYER);
            if (wall.isEnabled(face, DisplayOption.COUNT)) {
                String count = IModHelpers.get().getGuiHelpers().quantityToScaledString(stats.count());
                ChestOverlayHelpers.renderText(poseStack, buffers, count, 0.5F, TEXT_Y0, TEXT_HEIGHT, COLOR_TEXT, light);
            }
            if (wall.isEnabled(face, DisplayOption.FILL_LEVEL)) {
                renderFillBar(atlas.getSprite(BAR_TEXTURE), stats.getFillLevel(), poseStack, buffers, light, overlay);
            }
            if (wall.isEnabled(face, DisplayOption.UPGRADE_INDICATORS)) {
                renderIndicators(atlas, stats, poseStack, buffers, light, overlay);
            }
        }
        poseStack.popPose();
    }

    private void renderPanel(BlockEntityChestCore core, TextureAtlas atlas, PoseStack poseStack, MultiBufferSource buffers,
                             int light, int overlay) {
        // A frame of the chest's material, so the panel looks built into it.
        if (core.getBlockState().getBlock() instanceof BlockChestCore block) {
            ResourceLocation frame = block.getMaterial().id().withPrefix("block/chest_wall_");
            TextureAtlasSprite frameSprite = atlas.getSprite(frame);
            ChestOverlayHelpers.renderSprite(poseStack, buffers, frameSprite, 0, 0, 1, 1, light, overlay);
            ChestOverlayHelpers.renderSides(poseStack, buffers, frameSprite, RenderChestCore.OVERLAY_OFFSET, light, overlay);
            poseStack.translate(0, 0, LAYER);
        }
        // The panel texture has a transparent rim, so the frame shows around it.
        ChestOverlayHelpers.renderSprite(poseStack, buffers, atlas.getSprite(PANEL_TEXTURE), 0, 0, 1, 1, light, overlay);
    }

    private void renderDisplayedItem(PoseStack poseStack, MultiBufferSource buffers, Level level, ItemStack stack, int light) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, level, null, 0);
        poseStack.pushPose();
        poseStack.translate(0.5F, ITEM_Y, 0);
        if (GeneralConfig.displayItemFrameStyle && model.isGui3d()) {
            // Like an item frame: blocks show their front face, with some depth.
            poseStack.scale(ITEM_SIZE * 1.6F, ITEM_SIZE * 1.6F, ITEM_SIZE * 0.4F);
            Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.FIXED, false, poseStack, buffers, light,
                    OverlayTexture.NO_OVERLAY, model);
        } else {
            // Like drawers: the inventory icon, flattened towards the face so blocks do not stick out.
            poseStack.scale(ITEM_SIZE, ITEM_SIZE, 0.001F);
            Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, poseStack, buffers, light,
                    OverlayTexture.NO_OVERLAY, model);
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
