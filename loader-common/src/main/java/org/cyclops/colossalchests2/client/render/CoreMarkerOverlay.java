package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;

/**
 * Marks where the core of a formed chest is, while the player sneaks.
 * @author rubensworks
 */
public class CoreMarkerOverlay implements IChestOverlay {

    public static final CoreMarkerOverlay INSTANCE = new CoreMarkerOverlay();

    private static final float MIN = 5F / 16F;
    private static final float MAX = 11F / 16F;

    @Override
    public void render(BlockEntityChestCore core, BlockPos pos, Direction face, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (!(core.getBlockState().getBlock() instanceof BlockChestCore block) || !isVisible()) {
            return;
        }
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/chest_core_" + block.getMaterial().getName());
        ChestOverlayHelpers.renderSprite(poseStack, buffers, Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture),
                MIN, MIN, MAX, MAX, light, overlay);
    }

    protected boolean isVisible() {
        return Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCrouching();
    }

}
