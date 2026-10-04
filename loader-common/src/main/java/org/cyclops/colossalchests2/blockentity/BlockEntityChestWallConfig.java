package org.cyclops.colossalchests2.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests2.block.BlockChestFunctionalWall;
import org.cyclops.colossalchests2.client.render.ChestOverlays;
import org.cyclops.colossalchests2.client.render.FunctionalWallOverlay;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.Set;

/**
 * Config for the {@link BlockEntityChestWall}.
 * @author rubensworks
 */
public class BlockEntityChestWallConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityChestWall, M> {

    public BlockEntityChestWallConfig(M mod, BlockEntityType.BlockEntitySupplier<? extends BlockEntityChestWall> supplier) {
        super(
                mod,
                "chest_wall",
                eConfig -> new BlockEntityType<>(supplier, Set.copyOf(BlockChestFunctionalWall.getFunctionalInstances()), null)
        );
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        if (getMod().getModHelpers().getMinecraftHelpers().isClientSide()) {
            for (BlockChestFunctionalWall wall : BlockChestFunctionalWall.getFunctionalInstances()) {
                ChestOverlays.register(wall, new FunctionalWallOverlay(wall.getType()));
            }
        }
    }

}
