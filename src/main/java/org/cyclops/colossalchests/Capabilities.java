package org.cyclops.colossalchests;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;

/**
 * Used capabilities for this mod.
 * @author rubensworks
 */
public class Capabilities {
    public static Capability<IInventoryState> INVENTORY_STATE = CapabilityManager.get(new CapabilityToken<>(){});
    public static Capability<ISlotlessItemHandler> SLOTLESS_ITEMHANDLER = CapabilityManager.get(new CapabilityToken<>(){});
}
