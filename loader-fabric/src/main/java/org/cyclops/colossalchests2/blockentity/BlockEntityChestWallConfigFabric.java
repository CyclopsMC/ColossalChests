package org.cyclops.colossalchests2.blockentity;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import org.cyclops.cyclopscore.init.ModBaseFabric;

/**
 * Fabric config for the {@link BlockEntityChestWall}, exposing item storage on formed functional walls.
 * @author rubensworks
 */
public class BlockEntityChestWallConfigFabric<M extends ModBaseFabric<?>> extends BlockEntityChestWallConfig<M> {

    public BlockEntityChestWallConfigFabric(M mod) {
        super(mod, BlockEntityChestWall::new);
    }

    @Override
    public void onForgeRegistered() {
        super.onForgeRegistered();
        ItemStorage.SIDED.registerForBlockEntity((wall, side) -> wall.getCore()
                .map(core -> ((BlockEntityChestCoreFabric) core).getFabricStorage().withAccess(wall.getAccess()))
                .orElse(null), getInstance());
    }
}
