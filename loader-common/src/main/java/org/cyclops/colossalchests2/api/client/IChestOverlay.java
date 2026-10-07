package org.cyclops.colossalchests2.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.cyclops.colossalchests2.api.IChest;

/**
 * Draws something on an outer face of a chest member, on top of the giant chest.
 * Register instances in {@link ChestOverlays}.
 * @author rubensworks
 */
@FunctionalInterface
public interface IChestOverlay {

    /**
     * Render on one outer face of a member.
     * The pose maps the unit square from (0, 0) to (1, 1) onto the face as seen from outside:
     * x to the right, y up and z towards the viewer, already slightly in front of the chest surface.
     * On the top face, up points away from the chest front, on the bottom face towards it.
     * Faces of members on the lid move along with it.
     * @param chest The chest.
     * @param pos The member position.
     * @param face The outer face being rendered.
     * @param partialTick The partial tick.
     * @param poseStack The pose stack.
     * @param buffers The buffers.
     * @param light The packed light in front of this face.
     * @param overlay The packed overlay.
     */
    void render(IChest chest, BlockPos pos, Direction face, float partialTick,
                PoseStack poseStack, MultiBufferSource buffers, int light, int overlay);

}
