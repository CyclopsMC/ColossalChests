package org.cyclops.colossalchests2.proxy;

import org.cyclops.colossalchests2.ColossalChests;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return ColossalChests._instance;
    }


}
