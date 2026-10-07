package org.cyclops.colossalchests2.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.block.WallType;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;

/**
 * Shows a functional wall's icon on its outer faces of the giant chest while the player sneaks, like the core marker.
 * @author rubensworks
 */
public class FunctionalWallOverlay implements IChestOverlay {

    private static final float MIN = 3F / 16F;
    private static final float MAX = 13F / 16F;

    private final ResourceLocation texture;

    public FunctionalWallOverlay(WallType type) {
        this(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "block/" + type.getRegistryName() + "_icon"));
    }

    /**
     * @param texture A block atlas texture, such as othermod:block/chest_wall_example_icon.
     */
    public FunctionalWallOverlay(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public void render(BlockEntityChestCore core, BlockPos pos, Direction face, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (!ChestOverlayHelpers.isRevealingMembers()) {
            return;
        }
        ChestOverlayHelpers.renderSprite(poseStack, buffers, Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture),
                MIN, MIN, MAX, MAX, light, overlay);
    }

}
