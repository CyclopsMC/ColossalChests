package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.client.gui.GuiHopper;
import net.minecraft.entity.player.InventoryPlayer;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

/**
 * @author rubensworks
 */
public class GuiUncolossalChest extends GuiHopper {
    public GuiUncolossalChest(InventoryPlayer playerInv, TileUncolossalChest tile) {
        super(playerInv, tile);
    }
}
