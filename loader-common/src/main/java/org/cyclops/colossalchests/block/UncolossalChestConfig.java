package org.cyclops.colossalchests.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.blockentity.CyclopsBlockEntity;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.function.BiFunction;

/**
 * Config for the {@link ColossalChest}.
 * @author rubensworks
 *
 */
public abstract class UncolossalChestConfig<M extends IModBase> extends BlockConfigCommon<M> {

    public UncolossalChestConfig(M mod) {
        super(
                mod,
                "uncolossal_chest",
                (eConfig, properties) -> new UncolossalChest(((UncolossalChestConfig<M>) eConfig).getProperties(),
                        ((UncolossalChestConfig<M>) eConfig).getBlockEntitySupplier()),
                getDefaultItemConstructor(mod)
        );
    }

    /**
     * Loaders can override this to change which block entity is created for this block.
     * @return The block entity supplier.
     */
    protected BiFunction<BlockPos, BlockState, ? extends CyclopsBlockEntity> getBlockEntitySupplier() {
        return BlockEntityUncolossalChest::new;
    }

    public Block.Properties getProperties() {
        return Block.Properties.of()
                .setId((ResourceKey<Block>) getResourceKey())
                .overrideDescription("block.colossalchests.interface")
                .strength(5.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD);
    }
}
