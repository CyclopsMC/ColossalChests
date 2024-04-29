package org.cyclops.colossalchests.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.cyclops.colossalchests.GeneralConfig;

/**
 * A recipe condition for checking if the {@link GeneralConfig#metalVariants} setting is enabled.
 * @author rubensworks
 */
public record ConditionMetalVariantsSetting() implements ICondition {

    public static final Codec<ConditionMetalVariantsSetting> CODEC = RecordCodecBuilder.create(
            builder -> builder.point(new ConditionMetalVariantsSetting())
    );

    @Override
    public boolean test(IContext context) {
        return GeneralConfig.metalVariants;
    }

    @Override
    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

}
