package org.cyclops.colossalchests2.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.cyclops.colossalchests2.GeneralConfig;

import java.util.List;

/**
 * Tunable properties of a chest material, loaded from data/[namespace]/colossalchests2/material/[name].json.
 * All fields are optional, so fields added later never break existing datapacks.
 * Per-material upgrade limits are declared by each upgrade, not here.
 * @param upgradeSlots Number of core upgrade slots.
 * @param maxSize Maximum structure size (outer edge length).
 * @param blastResistant If the blocks resist explosions.
 * @param upgradeCost What a Material Upgrade takes per plain wall and core to turn a chest of the previous material
 *                    into this one. Downgrading refunds it.
 * @author rubensworks
 */
public record MaterialProperties(int upgradeSlots, int maxSize, boolean blastResistant, List<MaterialCost> upgradeCost) {

    public static final MaterialProperties DEFAULT = new MaterialProperties(1, 3, false);

    public MaterialProperties {
        upgradeCost = List.copyOf(upgradeCost);
    }

    public MaterialProperties(int upgradeSlots, int maxSize, boolean blastResistant) {
        this(upgradeSlots, maxSize, blastResistant, List.of());
    }

    public static final Codec<MaterialProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(0, 64).optionalFieldOf("upgrade_slots", DEFAULT.upgradeSlots()).forGetter(MaterialProperties::upgradeSlots),
            Codec.intRange(GeneralConfig.MIN_SIZE, GeneralConfig.HARD_MAX_SIZE).optionalFieldOf("max_size", DEFAULT.maxSize()).forGetter(MaterialProperties::maxSize),
            Codec.BOOL.optionalFieldOf("blast_resistant", DEFAULT.blastResistant()).forGetter(MaterialProperties::blastResistant),
            MaterialCost.CODEC.listOf().optionalFieldOf("upgrade_cost", DEFAULT.upgradeCost()).forGetter(MaterialProperties::upgradeCost)
    ).apply(i, MaterialProperties::new));

}
