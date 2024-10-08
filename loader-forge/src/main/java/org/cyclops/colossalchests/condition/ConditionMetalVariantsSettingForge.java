package org.cyclops.colossalchests.condition;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.cyclops.colossalchests.GeneralConfig;

/**
 * A recipe condition for checking if the {@link GeneralConfig#metalVariants} setting is enabled.
 * @author rubensworks
 */
public record ConditionMetalVariantsSettingForge() implements ICondition {

    public static final MapCodec<ConditionMetalVariantsSettingForge> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.point(new ConditionMetalVariantsSettingForge())
    );

    @Override
    public boolean test(IContext context, DynamicOps<?> dynamicOps) {
        return GeneralConfig.metalVariants;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

}
