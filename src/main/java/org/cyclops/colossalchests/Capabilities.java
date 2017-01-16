package org.cyclops.colossalchests;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;

/**
 * Used capabilities for this mod.
 * @author rubensworks
 */
public class Capabilities {
    @CapabilityInject(IInventoryState.class)
    public static Capability<IInventoryState> INVENTORY_STATE = null;
    @CapabilityInject(ISlotlessItemHandler.class)
    public static Capability<ISlotlessItemHandler> SLOTLESS_ITEMHANDLER = null;
}
