package org.cyclops.colossalchests.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChestFabric;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.init.ModBaseFabric;

import java.util.function.BiFunction;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public class UncolossalChestConfigFabric<M extends ModBaseFabric> extends UncolossalChestConfig<M> {

    public UncolossalChestConfigFabric(M mod) {
        super(mod);
    }

    @Override
    protected BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntity> getBlockEntitySupplier() {
        return BlockEntityUncolossalChestFabric::new;
    }

    // TODO: register custom item special renderer once Fabric supports this. Then we can modify the items model JSON file.
}
