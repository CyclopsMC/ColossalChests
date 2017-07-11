package org.cyclops.colossalchests;

import org.cyclops.colossalchests.advancement.criterion.ChestFormedTrigger;
import org.cyclops.cyclopscore.helper.AdvancementHelpers;

/**
 * Advancement-related logic.
 * @author rubensworks
 */
public class Advancements {

    public static final ChestFormedTrigger CHEST_FORMED = AdvancementHelpers
            .registerCriteriaTrigger(new ChestFormedTrigger());

    public static void load() {}

}
