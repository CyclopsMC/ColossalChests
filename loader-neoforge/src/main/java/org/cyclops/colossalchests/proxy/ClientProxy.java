package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChestsNeoForge;
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
    public ModBaseNeoForge<ColossalChestsNeoForge> getMod() {
        return ColossalChestsNeoForge._instance;
    }

}
