package org.cyclops.colossalchests.inventory.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.cyclopscore.inventory.container.InventoryContainerCommon;

/**
 * @author rubensworks
 */
public class ContainerUncolossalChest extends InventoryContainerCommon {

    public ContainerUncolossalChest(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, new SimpleContainer(5));
    }

    public ContainerUncolossalChest(int id, Inventory playerInventory, Container inventory) {
        super(RegistryEntries.CONTAINER_UNCOLOSSAL_CHEST.value(), id, playerInventory, inventory);

        this.addInventory(inventory, 0, 44, 20, 1, getSizeInventory());
        this.addPlayerInventory(playerInventory, 8, 51);
    }

    @Override
    protected int getSizeInventory() {
        return 5;
    }

}
