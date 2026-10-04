package org.cyclops.colossalchests2.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Per-chest settings from the GUI's settings tab. Stored on the core and kept on the core item.
 * @param showFillLevels If display walls show how full their slot is.
 * @param showCounts If display walls show item counts.
 * @param showUpgradeIndicators If display walls show which upgrades are applied.
 * @author rubensworks
 */
public record ChestSettings(boolean showFillLevels, boolean showCounts, boolean showUpgradeIndicators) {

    public static final ChestSettings DEFAULT = new ChestSettings(true, true, true);

    public static final Codec<ChestSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.optionalFieldOf("show_fill_levels", DEFAULT.showFillLevels()).forGetter(ChestSettings::showFillLevels),
            Codec.BOOL.optionalFieldOf("show_counts", DEFAULT.showCounts()).forGetter(ChestSettings::showCounts),
            Codec.BOOL.optionalFieldOf("show_upgrade_indicators", DEFAULT.showUpgradeIndicators()).forGetter(ChestSettings::showUpgradeIndicators)
    ).apply(i, ChestSettings::new));

    public static final StreamCodec<ByteBuf, ChestSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ChestSettings::showFillLevels,
            ByteBufCodecs.BOOL, ChestSettings::showCounts,
            ByteBufCodecs.BOOL, ChestSettings::showUpgradeIndicators,
            ChestSettings::new);

    public ChestSettings withShowFillLevels(boolean value) {
        return new ChestSettings(value, showCounts, showUpgradeIndicators);
    }

    public ChestSettings withShowCounts(boolean value) {
        return new ChestSettings(showFillLevels, value, showUpgradeIndicators);
    }

    public ChestSettings withShowUpgradeIndicators(boolean value) {
        return new ChestSettings(showFillLevels, showCounts, value);
    }
}
