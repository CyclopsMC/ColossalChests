package org.cyclops.colossalchests2.proxy;

import org.cyclops.colossalchests2.ColossalChestsFabric;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentFabric;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyFabric extends CommonProxyComponentFabric {

    @Override
    public ModBaseFabric<?> getMod() {
        return ColossalChestsFabric._instance;
    }

}
