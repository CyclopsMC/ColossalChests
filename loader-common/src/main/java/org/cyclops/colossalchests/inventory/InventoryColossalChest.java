package org.cyclops.colossalchests.inventory;

import net.minecraft.world.entity.ContainerUser;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.cyclopscore.inventory.IndexedInventory;

/**
 * The inventory that holds a colossal chest's contents, which forwards opening and closing to the chest.
 * @author rubensworks
 */
public class InventoryColossalChest extends IndexedInventory {

    private final BlockEntityColossalChest chest;

    public InventoryColossalChest(BlockEntityColossalChest chest, int size, int stackLimit) {
        super(size, stackLimit);
        this.chest = chest;
    }

    @Override
    public void startOpen(ContainerUser entityPlayer) {
        super.startOpen(entityPlayer);
        this.chest.startOpen(entityPlayer);
    }

    @Override
    public void stopOpen(ContainerUser entityPlayer) {
        super.stopOpen(entityPlayer);
        this.chest.stopOpen(entityPlayer);
    }
}
