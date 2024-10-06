package org.cyclops.colossalchests.advancement.criterion;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.CriterionTriggerConfig;

/**
 * @author rubensworks
 *
 */
public class ChestFormedTriggerConfig extends CriterionTriggerConfig<ChestFormedTrigger.Instance> {

    /**
     * Make a new instance.
     */
    public ChestFormedTriggerConfig() {
        super(
                ColossalChests._instance,
                "chest_formed",
                new ChestFormedTrigger()
        );
    }

}
