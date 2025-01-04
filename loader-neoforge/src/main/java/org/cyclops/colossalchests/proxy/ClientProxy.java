package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.proxy.ClientProxyComponent;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxy extends ClientProxyComponent {

    public ClientProxy() {
        super(new CommonProxy());
    }

    @Override
    public ModBaseNeoForge<ColossalChests> getMod() {
        return ColossalChests._instance;
    }

}
