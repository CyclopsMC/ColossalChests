package org.cyclops.colossalchests.block;

import net.minecraft.world.level.Level;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;
import org.cyclops.cyclopscore.events.IBlockExplodedEvent;

/**
 * @author rubensworks
 */
public class InterfaceFabric extends Interface {
    public InterfaceFabric(Properties properties, ChestMaterial material) {
        super(properties, material, BlockEntityInterface::new);
        IBlockExplodedEvent.EVENT.register((blockState, level, blockPos, explosion, biConsumer) -> onBlockExplodedCommon(blockState, level, blockPos, explosion));
    }

    @Override
    protected boolean isCaptureBlockSnapshots(Level level) {
        return false;
    }
}
