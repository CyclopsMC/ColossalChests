package org.cyclops.colossalchests.condition;

import org.cyclops.colossalchests.ColossalChestsFabric;
import org.cyclops.cyclopscore.config.extendedconfig.ConditionConfigFabric;

/**
 * Config for the metal variants setting recipe condition.
 * @author rubensworks
 */
public class ConditionMetalVariantsSettingConfig extends ConditionConfigFabric<ConditionMetalVariantsSetting> {

    public ConditionMetalVariantsSettingConfig() {
        super(
                ColossalChestsFabric._instance,
                "metal_variants_enabled",
                ConditionMetalVariantsSetting.TYPE
        );
    }

}
