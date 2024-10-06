package org.cyclops.colossalchests.client.render.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;

/**
 * @author rubensworks
 */
public class RenderTileEntityColossalChestNeoForge extends RenderTileEntityColossalChest {
    public RenderTileEntityColossalChestNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityColossalChest blockEntity) {
        int size = blockEntity.getSizeSingular();
        return new AABB(
                Vec3.atLowerCornerOf(blockEntity.getBlockPos().subtract(new Vec3i(size, size, size))),
                Vec3.atLowerCornerOf(blockEntity.getBlockPos().offset(size, size * 2, size))
        );
    }
}
