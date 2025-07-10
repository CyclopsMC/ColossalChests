package org.cyclops.colossalchests.condition;

import org.cyclops.colossalchests.ColossalChestsNeoForge;
import org.cyclops.cyclopscore.config.extendedconfig.ConditionConfigNeoForge;

/**
 * Config for the metal variants setting recipe condition.
 * @author rubensworks
 */
public class ConditionMetalVariantsSettingConfig extends ConditionConfigNeoForge<ConditionMetalVariantsSetting> {

    public ConditionMetalVariantsSettingConfig() {
        super(
                ColossalChestsNeoForge._instance,
                "metal_variants_enabled",
                ConditionMetalVariantsSetting.CODEC
        );
    }

}
