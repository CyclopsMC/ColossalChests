package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HopperMenu;

/**
 * The vanilla hopper menu, which also tells the uncolossal chest whose menu is open.
 * Clients see a plain hopper menu with the vanilla hopper screen.
 * @author rubensworks
 */
public class ContainerUncolossalChest extends HopperMenu {

    private final Container container;

    public ContainerUncolossalChest(int containerId, Inventory inventory, Container container) {
        super(containerId, inventory, container);
        this.container = container;
    }

    public Container getContainer() {
        return container;
    }

}
