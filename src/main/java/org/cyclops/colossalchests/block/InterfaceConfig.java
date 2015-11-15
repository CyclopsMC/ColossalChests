package org.cyclops.colossalchests.block;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.BlockContainerConfig;

/**
 * Config for the {@link Interface}.
 * @author rubensworks
 *
 */
public class InterfaceConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static InterfaceConfig _instance;

    /**
     * Make a new instance.
     */
    public InterfaceConfig() {
        super(
                ColossalChests._instance,
        	true,
            "interface",
            null,
            Interface.class
        );
    }
    
    @Override
    public boolean isMultipartEnabled() {
        return true;
    }
    
}
