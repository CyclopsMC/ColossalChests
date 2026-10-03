package org.cyclops.colossalchests2.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.colossalchests2.api.IDeepItemStorage;

/**
 * Capabilities added by this mod on Forge.
 * @author rubensworks
 */
public class CapabilitiesForge {

    public static final Capability<IDeepItemStorage> DEEP_ITEM_STORAGE = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IDeepItemStorage.class);
    }

}
