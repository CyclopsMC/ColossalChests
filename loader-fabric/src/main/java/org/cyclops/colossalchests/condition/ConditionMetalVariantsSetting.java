package org.cyclops.colossalchests.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests.GeneralConfig;
import org.cyclops.colossalchests.Reference;
import org.jetbrains.annotations.Nullable;

/**
 * A recipe condition for checking if the {@link GeneralConfig#metalVariants} setting is enabled.
 * @author rubensworks
 */
public record ConditionMetalVariantsSetting() implements ResourceCondition {

    public static final MapCodec<ConditionMetalVariantsSetting> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.point(new ConditionMetalVariantsSetting())
    );
    public static final ResourceConditionType<ConditionMetalVariantsSetting> TYPE = ResourceConditionType.create(
            ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "metal_variants_enabled"),
            CODEC
    );

    @Override
    public ResourceConditionType<?> getType() {
        return TYPE;
    }

    @Override
    public boolean test(@Nullable HolderLookup.Provider registryLookup) {
        return GeneralConfig.metalVariants;
    }
}
