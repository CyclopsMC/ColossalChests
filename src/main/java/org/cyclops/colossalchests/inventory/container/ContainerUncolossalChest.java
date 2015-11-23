package org.cyclops.colossalchests.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

/**
 * @author rubensworks
 */
public class ContainerUncolossalChest extends ContainerHopper {
    public ContainerUncolossalChest(InventoryPlayer playerInventory, TileUncolossalChest tile) {
        super(playerInventory, tile, playerInventory.player);
    }
}
