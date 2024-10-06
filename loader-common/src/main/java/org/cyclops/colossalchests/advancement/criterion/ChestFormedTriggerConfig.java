package org.cyclops.colossalchests.advancement.criterion;

import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * @author rubensworks
 *
 */
public class ChestFormedTriggerConfig<M extends IModBase> extends CriterionTriggerConfigCommon<ChestFormedTrigger.Instance, M> {

    /**
     * Make a new instance.
     */
    public ChestFormedTriggerConfig(M mod) {
        super(
                mod,
                "chest_formed",
                new ChestFormedTrigger()
        );
    }

}
