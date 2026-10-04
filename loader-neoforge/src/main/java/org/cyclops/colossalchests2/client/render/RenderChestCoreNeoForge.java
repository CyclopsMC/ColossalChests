package org.cyclops.colossalchests2.client.render;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;

/**
 * NeoForge chest renderer, which is culled by the bounds of the whole giant chest.
 * @author rubensworks
 */
public class RenderChestCoreNeoForge extends RenderChestCore {

    public RenderChestCoreNeoForge(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityChestCore core) {
        return core.getRenderBounds();
    }
}
