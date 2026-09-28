package org.cyclops.colossalchests2.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tunable values of the core upgrades.
 * @param baseSlots Slots without Slot Expansion upgrades.
 * @param slotsPerExpansion Slots added per Slot Expansion upgrade.
 * @param maxSlots Maximum slot count.
 * @param depthMultiplier Depth multiplier per Depth upgrade.
 * @param maxBundlingLevel Maximum Bundling upgrade level.
 * @author rubensworks
 */
public record UpgradeValues(int baseSlots, int slotsPerExpansion, int maxSlots, int depthMultiplier, int maxBundlingLevel) {

    public static final Codec<UpgradeValues> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, 81).fieldOf("base_slots").forGetter(UpgradeValues::baseSlots),
            Codec.intRange(0, 81).fieldOf("slots_per_expansion").forGetter(UpgradeValues::slotsPerExpansion),
            Codec.intRange(1, 81).fieldOf("max_slots").forGetter(UpgradeValues::maxSlots),
            Codec.intRange(1, 1024).fieldOf("depth_multiplier").forGetter(UpgradeValues::depthMultiplier),
            Codec.intRange(0, 16).fieldOf("max_bundling_level").forGetter(UpgradeValues::maxBundlingLevel)
    ).apply(i, UpgradeValues::new));

    public static final UpgradeValues DEFAULT = new UpgradeValues(27, 27, 81, 2, 4);

}
