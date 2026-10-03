package org.cyclops.colossalchests2.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tunable properties of a chest material, loaded from data/[namespace]/colossalchests2/material/[name].json.
 * All fields are optional, so fields added later never break existing datapacks.
 * Per-material upgrade limits are declared by each upgrade, not here.
 * @param upgradeSlots Number of core upgrade slots.
 * @param maxSize Maximum structure size (outer edge length).
 * @param blastResistant If the blocks resist explosions.
 * @author rubensworks
 */
public record MaterialProperties(int upgradeSlots, int maxSize, boolean blastResistant) {

    public static final MaterialProperties DEFAULT = new MaterialProperties(1, 3, false);

    public static final Codec<MaterialProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 64).optionalFieldOf("upgrade_slots", DEFAULT.upgradeSlots()).forGetter(MaterialProperties::upgradeSlots),
            Codec.intRange(2, ChestProperties.HARD_MAX_SIZE).optionalFieldOf("max_size", DEFAULT.maxSize()).forGetter(MaterialProperties::maxSize),
            Codec.BOOL.optionalFieldOf("blast_resistant", DEFAULT.blastResistant()).forGetter(MaterialProperties::blastResistant)
    ).apply(i, MaterialProperties::new));

}
