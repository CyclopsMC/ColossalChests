package org.cyclops.colossalchests.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.cyclopscore.init.ModBaseForge;

/**
 * @author rubensworks
 */
public class BlockEntityColossalChestConfigForge<M extends ModBaseForge> extends BlockEntityColossalChestConfig<M> {
    public BlockEntityColossalChestConfigForge(M mod) {
        super(mod);
    }

    @Override
    protected BlockEntityType.BlockEntitySupplier<? extends BlockEntityColossalChest> getBlockEntitySupplier() {
        return BlockEntityColossalChestForge::new;
    }
}
