package org.cyclops.colossalchests2.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tunable properties of a chest material.
 * @param upgradeSlots Number of core upgrade slots.
 * @param maxDepthUpgrades Maximum number of Depth upgrades.
 * @param maxSize Maximum structure size (outer edge length).
 * @param blastResistant If the blocks resist explosions.
 * @author rubensworks
 */
public record MaterialProperties(int upgradeSlots, int maxDepthUpgrades, int maxSize, boolean blastResistant) {

    public static final Codec<MaterialProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 64).fieldOf("upgrade_slots").forGetter(MaterialProperties::upgradeSlots),
            Codec.intRange(0, 64).fieldOf("max_depth_upgrades").forGetter(MaterialProperties::maxDepthUpgrades),
            Codec.intRange(2, ChestTables.HARD_MAX_SIZE).fieldOf("max_size").forGetter(MaterialProperties::maxSize),
            Codec.BOOL.optionalFieldOf("blast_resistant", false).forGetter(MaterialProperties::blastResistant)
    ).apply(i, MaterialProperties::new));

}
