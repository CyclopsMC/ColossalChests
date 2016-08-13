package org.cyclops.colossalchests.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import invtweaks.api.container.ChestContainer;
import invtweaks.api.container.ContainerSection;
import invtweaks.api.container.ContainerSectionCallback;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.Slot;
import org.cyclops.colossalchests.tileentity.TileUncolossalChest;

import java.util.List;
import java.util.Map;

/**
 * @author rubensworks
 */
@ChestContainer(rowSize = 5)
public class ContainerUncolossalChest extends ContainerHopper {
    public ContainerUncolossalChest(InventoryPlayer playerInventory, TileUncolossalChest tile) {
        super(playerInventory, tile, playerInventory.player);
    }

    /**
     * @return Container selection options for inventory tweaks.
     */
    @ContainerSectionCallback
    public Map<ContainerSection, List<Slot>> getContainerSelection() {
        Map<ContainerSection, List<Slot>> selection = Maps.newHashMap();
        List<Slot> chest = Lists.newArrayList();
        List<Slot> playerInventory = Lists.newArrayList();
        for(int i = 0; i < 5; i++) {
            chest.add(this.getSlot(i));
        }
        for(int i = 5; i < 5 + 4 * 9; i++) {
            playerInventory.add(this.getSlot(i));
        }
        selection.put(ContainerSection.CHEST, chest);
        selection.put(ContainerSection.INVENTORY, playerInventory);
        return selection;
    }
}
