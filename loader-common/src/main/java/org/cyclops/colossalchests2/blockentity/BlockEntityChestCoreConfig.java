package org.cyclops.colossalchests2.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests2.block.BlockChestCore;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.Set;

/**
 * Config for the {@link BlockEntityChestCore}.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityChestCore, M> {

    public BlockEntityChestCoreConfig(M mod, BlockEntityType.BlockEntitySupplier<? extends BlockEntityChestCore> supplier) {
        super(
                mod,
                "chest_core",
                eConfig -> new BlockEntityType<>(supplier, Set.copyOf(BlockChestCore.getInstances()), null)
        );
    }

}
