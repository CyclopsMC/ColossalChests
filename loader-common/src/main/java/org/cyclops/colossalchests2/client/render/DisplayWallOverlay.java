package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.colossalchests2.storage.DisplayStats;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.cyclops.cyclopscore.helper.IModHelpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows a Display wall's item, count, fill level and upgrade indicators on its outer faces, honouring the chest's
 * visual settings. An empty Display wall shows its icon while the player sneaks, like other functional walls.
 * @author rubensworks
 */
public class DisplayWallOverlay implements IChestOverlay {

    private static final ResourceLocation BAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/display_bar");
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_BAR_BACKGROUND = 0xFF202020;
    private static final int COLOR_BAR = 0xFF40C040;
    private static final int COLOR_BAR_FULL = 0xFFD04030;
    private static final float BAR_X0 = 3F / 16F;
    private static final float BAR_X1 = 13F / 16F;
    private static final float BAR_Y0 = 1F / 16F;
    private static final float BAR_Y1 = 2F / 16F;
    private static final float INDICATOR_SIZE = 3F / 16F;

    private final FunctionalWallOverlay icon = new FunctionalWallOverlay(WallType.DISPLAY);

    @Override
    public void render(BlockEntityChestCore core, BlockPos pos, Direction face, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        Level level = core.getLevel();
        if (!(level.getBlockEntity(pos) instanceof BlockEntityChestWall wall) || wall.getDisplayed().isEmpty()) {
            icon.render(core, pos, face, partialTick, poseStack, buffers, light, overlay);
            return;
        }
        ChestSettings settings = core.getSettings();
        DisplayStats stats = wall.getDisplayStats();
        ChestOverlayHelpers.renderItem(poseStack, buffers, level, wall.getDisplayed(), 0.5F, 9F / 16F, 8F / 16F, light);
        if (settings.showCounts()) {
            String count = IModHelpers.get().getGuiHelpers().quantityToScaledString(stats.count());
            ChestOverlayHelpers.renderText(poseStack, buffers, count, 0.5F, 2.5F / 16F, 2.5F / 16F, COLOR_TEXT, light);
        }
        if (settings.showFillLevels()) {
            TextureAtlasSprite bar = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(BAR_TEXTURE);
            float fill = stats.getFillLevel();
            ChestOverlayHelpers.renderSprite(poseStack, buffers, bar, BAR_X0, BAR_Y0, BAR_X1, BAR_Y1, light, overlay, COLOR_BAR_BACKGROUND);
            if (fill > 0) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 0.001F);
                ChestOverlayHelpers.renderSprite(poseStack, buffers, bar, BAR_X0, BAR_Y0, BAR_X0 + (BAR_X1 - BAR_X0) * fill, BAR_Y1,
                        light, overlay, fill >= 1 ? COLOR_BAR_FULL : COLOR_BAR);
                poseStack.popPose();
            }
        }
        if (settings.showUpgradeIndicators()) {
            // Upgrades that affect the shown item, from the top right corner leftwards.
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
            float x = 1F - 1F / 16F - INDICATOR_SIZE / 2;
            for (ChestUpgrade upgrade : indicators) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "upgrade_" + upgrade.getId().getPath())));
                ChestOverlayHelpers.renderItem(poseStack, buffers, level, stack, x, 1F - 1F / 16F - INDICATOR_SIZE / 2, INDICATOR_SIZE, light);
                x -= INDICATOR_SIZE;
            }
        }
    }

}
