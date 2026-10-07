package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.cyclops.colossalchests2.GeneralConfig;

/**
 * Tunable properties of a chest material, loaded from data/[namespace]/colossalchests2/material/[name].json.
 * All fields are optional, so fields added later never break existing datapacks.
 * @param upgradeSlots Number of core upgrade slots.
 * @param maxSize Maximum structure size (outer edge length).
 * @param blastResistant If the blocks resist explosions.
 * @param upgradeLimits How many of an upgrade a chest takes, by upgrade id, for upgrades that do not set a limit
 *                      for this material themselves. This lets added materials set limits without changing upgrade files.
 * @author rubensworks
 */
public record MaterialProperties(int upgradeSlots, int maxSize, boolean blastResistant, Map<ResourceLocation, Integer> upgradeLimits) {

    public static final MaterialProperties DEFAULT = new MaterialProperties(1, 3, false);

    public static final Codec<MaterialProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 64).optionalFieldOf("upgrade_slots", DEFAULT.upgradeSlots()).forGetter(MaterialProperties::upgradeSlots),
            Codec.intRange(GeneralConfig.MIN_SIZE, GeneralConfig.HARD_MAX_SIZE).optionalFieldOf("max_size", DEFAULT.maxSize()).forGetter(MaterialProperties::maxSize),
            Codec.BOOL.optionalFieldOf("blast_resistant", DEFAULT.blastResistant()).forGetter(MaterialProperties::blastResistant),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.intRange(0, 64)).optionalFieldOf("upgrade_limits", Map.of())
                    .forGetter(MaterialProperties::upgradeLimits)
    ).apply(i, MaterialProperties::new));

    public MaterialProperties {
        upgradeLimits = ImmutableMap.copyOf(upgradeLimits);
    }

    public MaterialProperties(int upgradeSlots, int maxSize, boolean blastResistant) {
        this(upgradeSlots, maxSize, blastResistant, Map.of());
    }

}
