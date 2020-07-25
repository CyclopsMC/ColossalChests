package org.cyclops.colossalchests.recipe.condition;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.RecipeConditionConfig;

/**
 * Config for the metal variants setting recipe condition.
 * @author rubensworks
 */
public class RecipeConditionMetalVariantsSettingConfig extends RecipeConditionConfig<RecipeConditionMetalVariantsSetting> {

    public RecipeConditionMetalVariantsSettingConfig() {
        super(
                ColossalChests._instance,
                RecipeConditionMetalVariantsSetting.Serializer.INSTANCE
        );
    }

}
