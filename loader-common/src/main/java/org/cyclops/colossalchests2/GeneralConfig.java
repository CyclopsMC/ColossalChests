package org.cyclops.colossalchests2;

import org.cyclops.cyclopscore.config.extendedconfig.DummyConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * A config with general options for this mod.
 * @author rubensworks
 *
 */
public class GeneralConfig<M extends IModBase> extends DummyConfigCommon<M> {

    public GeneralConfig(M mod) {
        super(mod, "general");
    }

}
