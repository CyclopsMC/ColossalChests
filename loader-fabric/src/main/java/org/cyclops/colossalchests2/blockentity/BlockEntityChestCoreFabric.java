package org.cyclops.colossalchests2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.capability.ChestStorageFabric;

/**
 * Fabric chest core, with one item storage per core so all lookups share transaction snapshots.
 * @author rubensworks
 */
public class BlockEntityChestCoreFabric extends BlockEntityChestCore {

    private final ChestStorageFabric fabricStorage;

    public BlockEntityChestCoreFabric(BlockPos pos, BlockState state) {
        super(RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value(), pos, state);
        this.fabricStorage = new ChestStorageFabric(getStorage());
    }

    public ChestStorageFabric getFabricStorage() {
        return fabricStorage;
    }
}
