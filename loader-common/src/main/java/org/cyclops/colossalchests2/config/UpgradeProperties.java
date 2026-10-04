package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Tunable properties of a core upgrade, loaded from data/[namespace]/colossalchests2/upgrade/[name].json.
 * All fields are optional, so fields added later never break existing datapacks.
 * @param maxCount How many of the upgrade a chest takes, for materials without their own limit.
 * @param maxCountByMaterial How many of the upgrade a chest takes, by material id.
 * @param value The strength of one upgrade, for example the depth multiplier.
 * @author rubensworks
 */
public record UpgradeProperties(int maxCount, Map<ResourceLocation, Integer> maxCountByMaterial, long value) {

    public static final UpgradeProperties DISABLED = new UpgradeProperties(0, Map.of(), 1);

    public static final Codec<UpgradeProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 64).optionalFieldOf("max_count", 1).forGetter(UpgradeProperties::maxCount),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.intRange(0, 64)).optionalFieldOf("max_count_by_material", Map.of())
                    .forGetter(UpgradeProperties::maxCountByMaterial),
            Codec.LONG.validate(value -> value >= 1 ? DataResult.success(value) : DataResult.error(() -> "value must be at least 1: " + value))
                    .optionalFieldOf("value", 1L).forGetter(UpgradeProperties::value)
    ).apply(i, UpgradeProperties::new));

    public UpgradeProperties {
        maxCountByMaterial = ImmutableMap.copyOf(maxCountByMaterial);
    }

    /**
     * @param material A material id.
     * @return How many of the upgrade a chest of that material takes.
     */
    public int getMaxCount(ResourceLocation material) {
        return maxCountByMaterial.getOrDefault(material, maxCount);
    }

}
