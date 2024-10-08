package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChestsForge;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.ClientProxyComponentForge;

/**
 * Proxy for the client side.
 *
 * @author rubensworks
 *
 */
public class ClientProxyForge extends ClientProxyComponentForge {

    public ClientProxyForge() {
        super(new CommonProxyForge());
    }

    @Override
    public ModBaseForge<?> getMod() {
        return ColossalChestsForge._instance;
    }

}
