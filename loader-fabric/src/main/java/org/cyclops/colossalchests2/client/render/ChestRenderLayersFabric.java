package org.cyclops.colossalchests2.client.render;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestWall;

/**
 * Fabric ignores render types in model files, so the invisible formed models need the cutout layer set here.
 * This runs once the client started, so walls and cores that other mods add are included.
 * @author rubensworks
 */
public final class ChestRenderLayersFabric {

    private ChestRenderLayersFabric() {
    }

    public static void register() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
                BuiltInRegistries.BLOCK.stream()
                        .filter(block -> block instanceof BlockChestWall || block instanceof BlockChestCore)
                        .toArray(Block[]::new)));
    }
}
