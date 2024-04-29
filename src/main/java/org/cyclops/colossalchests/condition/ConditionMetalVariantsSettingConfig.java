package org.cyclops.colossalchests.condition;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.ConditionConfig;

/**
 * Config for the metal variants setting recipe condition.
 * @author rubensworks
 */
public class ConditionMetalVariantsSettingConfig extends ConditionConfig<ConditionMetalVariantsSetting> {

    public ConditionMetalVariantsSettingConfig() {
        super(
                ColossalChests._instance,
                "metal_variants_enabled",
                ConditionMetalVariantsSetting.CODEC
        );
    }

}
