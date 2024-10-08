package org.cyclops.colossalchests.block;

import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.events.IBlockExplodedEvent;

/**
 * @author rubensworks
 */
public class ChestWallFabric extends ChestWall {
    public ChestWallFabric(Properties properties, ChestMaterial material) {
        super(properties, material);
        IBlockExplodedEvent.EVENT.register((blockState, level, blockPos, explosion, biConsumer) -> onBlockExplodedCommon(blockState, level, blockPos, explosion));
    }

    @Override
    protected boolean isCaptureBlockSnapshots(Level level) {
        return false;
    }
}
