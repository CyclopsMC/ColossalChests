package org.cyclops.colossalchests.condition;

import org.cyclops.colossalchests.ColossalChestsForge;
import org.cyclops.cyclopscore.config.extendedconfig.ConditionConfigForge;

/**
 * Config for the metal variants setting recipe condition.
 * @author rubensworks
 */
public class ConditionMetalVariantsSettingConfigForge extends ConditionConfigForge<ConditionMetalVariantsSettingForge> {

    public ConditionMetalVariantsSettingConfigForge() {
        super(
                ColossalChestsForge._instance,
                "metal_variants_enabled",
                ConditionMetalVariantsSettingForge.CODEC
        );
    }

}
