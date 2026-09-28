package org.cyclops.colossalchests2.storage;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.BeforeClass;

/**
 * Base class for tests that need vanilla registries.
 * @author rubensworks
 */
public abstract class BootstrapTest {

    @BeforeClass
    public static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

}
