package org.cyclops.colossalchests.recipe.condition;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.Reference;

/**
 * A recipe condition for checking if the {@link GeneralConfig#metalVariants} setting is enabled.
 * @author rubensworks
 */
public class RecipeConditionMetalVariantsSetting implements ICondition {

    private static final ResourceLocation NAME = new ResourceLocation(Reference.MOD_ID, "metal_variants_enabled");

    @Override
    public ResourceLocation getID() {
        return NAME;
    }

    @Override
    public boolean test(IContext context) {
        return GeneralConfig.metalVariants;
    }

    public static class Serializer implements IConditionSerializer<RecipeConditionMetalVariantsSetting> {

        public static final RecipeConditionMetalVariantsSetting.Serializer INSTANCE = new RecipeConditionMetalVariantsSetting.Serializer();

        @Override
        public void write(JsonObject json, RecipeConditionMetalVariantsSetting value) {

        }

        @Override
        public RecipeConditionMetalVariantsSetting read(JsonObject json) {
            return new RecipeConditionMetalVariantsSetting();
        }

        @Override
        public ResourceLocation getID() {
            return RecipeConditionMetalVariantsSetting.NAME;
        }
    }

}
