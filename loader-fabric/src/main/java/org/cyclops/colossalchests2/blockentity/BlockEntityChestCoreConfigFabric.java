package org.cyclops.colossalchests2.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.level.block.Block;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.client.render.ChestRenderLayersFabric;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * Fabric config for the {@link BlockEntityChestCore}, exposing item storage on formed cores and walls.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfigFabric<M extends ModBaseFabric<?>> extends BlockEntityChestCoreConfig<M> {

    public BlockEntityChestCoreConfigFabric(M mod) {
        super(mod, BlockEntityChestCoreFabric::new);
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            ChestRenderLayersFabric.register();
        }
        ItemStorage.SIDED.registerForBlockEntity((core, side) -> core.isFormed() ? ((BlockEntityChestCoreFabric) core).getFabricStorage() : null, getInstance());
        if (BlockChestWall.EXPOSES_CAPABILITIES) {
            ItemStorage.SIDED.registerForBlocks((level, pos, state, blockEntity, side) -> {
                if (!state.getValue(BlockChestWall.FORMED)) {
                    return null;
                }
                return ChestCoreIndex.findFormedCore(level, pos)
                        .map(core -> ((BlockEntityChestCoreFabric) core).getFabricStorage())
                        .orElse(null);
            }, BlockChestWall.getInstances().toArray(Block[]::new));
        }
    }
}
