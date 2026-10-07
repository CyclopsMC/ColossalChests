package org.cyclops.colossalchests2.advancement;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ChestFormedTrigger}.
 * @author rubensworks
 */
public class ChestFormedTriggerConfig<M extends IModBase> extends CriterionTriggerConfigCommon<ChestFormedTrigger.Instance, M> {

    public ChestFormedTriggerConfig(M mod) {
        super(mod, "chest_formed", new ChestFormedTrigger());
    }

}
