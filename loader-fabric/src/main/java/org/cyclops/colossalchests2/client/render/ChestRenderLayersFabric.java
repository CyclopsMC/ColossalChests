package org.cyclops.colossalchests2.client.render;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWall;
import org.cyclops.colossalchests2.block.BlockChestWall;

/**
 * Fabric ignores render types in model files, so the invisible formed models need the cutout layer set here.
 * @author rubensworks
 */
public final class ChestRenderLayersFabric {

    private ChestRenderLayersFabric() {
    }

    public static void register() {
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), BlockChestWall.getInstances().toArray(Block[]::new));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), BlockChestCore.getInstances().toArray(Block[]::new));
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), BlockChestFunctionalWall.getFunctionalInstances().toArray(Block[]::new));
    }
}
