package org.cyclops.colossalchests2.proxy;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.cyclops.colossalchests2.ColossalChestsFabric;
import org.cyclops.colossalchests2.block.DisplayWallInteractions;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.ClientProxyComponentFabric;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxyFabric extends ClientProxyComponentFabric {

    public ClientProxyFabric() {
        super(new CommonProxyFabric());
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        ClientTickEvents.END_CLIENT_TICK.register(minecraft ->
                DisplayWallInteractions.onClientTick(minecraft.options.keyAttack.isDown()));
    }

    @Override
    public ModBaseFabric<?> getMod() {
        return ColossalChestsFabric._instance;
    }
}
