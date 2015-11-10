package org.cyclops.colossalchests.block;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;

/**
 * Config for the {@link ChestWall}.
 * @author rubensworks
 *
 */
public class ChestWallConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static ChestWallConfig _instance;

    /**
     * Make a new instance.
     */
    public ChestWallConfig() {
        super(
                ColossalChests._instance,
        	true,
            "chestWall",
            null,
            ChestWall.class
        );
    }
    
    @Override
    public boolean isMultipartEnabled() {
        return true;
    }
    
}
