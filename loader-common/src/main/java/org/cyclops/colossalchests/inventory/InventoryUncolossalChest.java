package org.cyclops.colossalchests.inventory;

import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import org.cyclops.colossalchests.blockentity.BlockEntityUncolossalChest;
import org.cyclops.cyclopscore.inventory.SimpleInventory;

/**
 * The inventory that holds an uncolossal chest's contents, which forwards opening and closing to the chest.
 * @author rubensworks
 */
public class InventoryUncolossalChest extends SimpleInventory {

    private final BlockEntityUncolossalChest chest;

    public InventoryUncolossalChest(BlockEntityUncolossalChest chest, int size, int stackLimit) {
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

    @Override
    public boolean stillValid(Player entityplayer) {
        return super.stillValid(entityplayer)
                && this.chest.getLevel().getBlockEntity(this.chest.getBlockPos()) == this.chest;
    }
}
