package org.cyclops.colossalchests2.modcompat;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.colossalchests2.ColossalChests;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.commoncapabilities.api.capability.Capabilities;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.cyclopscore.modcompat.IModCompat;

/**
 * Exposes the inventory state of chests through Common Capabilities, so consumers can skip rescanning unchanged chests.
 * @author rubensworks
 */
public class CommonCapabilitiesModCompat implements IModCompat {

    @Override
    public String getId() {
        return Reference.MOD_COMMONCAPABILITIES;
    }

    @Override
    public boolean isEnabledDefault() {
        return true;
    }

    @Override
    public String getComment() {
        return "If the inventory state capability should be exposed on chests.";
    }

    @Override
    public ICompatInitializer createInitializer() {
        return () -> ColossalChests._instance.getModEventBus().addListener(CommonCapabilitiesModCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.InventoryState.BLOCK, RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value(),
                (core, side) -> core.isFormed() ? new InventoryStateChestStorage(core.getStorage()) : null);
        event.registerBlockEntity(Capabilities.InventoryState.BLOCK, RegistryEntries.BLOCK_ENTITY_CHEST_WALL.value(),
                (wall, side) -> wall.getCore().map(core -> (IInventoryState) new InventoryStateChestStorage(core.getStorage())).orElse(null));
    }
}
