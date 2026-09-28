package org.cyclops.colossalchests2.proxy;

import org.cyclops.colossalchests2.ColossalChestsForge;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentForge;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyForge extends CommonProxyComponentForge {

    @Override
    public ModBaseForge<?> getMod() {
        return ColossalChestsForge._instance;
    }

}
