package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

/**
 * Chest-wide values, loaded from data/colossalchests2/colossalchests2/chest.json.
 * All fields are optional, so fields added later never break existing datapacks.
 * @param baseSlots Slots without upgrades.
 * @param maxSlots Maximum slot count, the GUI never scrolls.
 * @param depthBySize Stacks per slot by structure size.
 * @author rubensworks
 */
public record ChestProperties(int baseSlots, int maxSlots, Map<Integer, Long> depthBySize) {

    public static final int HARD_MAX_SIZE = 10;
    public static final int HARD_MAX_SLOTS = 81;

    public static final ChestProperties DEFAULT = new ChestProperties(27, HARD_MAX_SLOTS,
            ImmutableMap.<Integer, Long>builder()
                    .put(2, 4L)
                    .put(3, 16L)
                    .put(4, 64L)
                    .put(5, 256L)
                    .put(6, 1024L)
                    .put(7, 4096L)
                    .put(8, 16384L)
                    .put(9, 65536L)
                    .put(10, 262144L)
                    .build());

    public static final Codec<Map<Integer, Long>> CODEC_DEPTH_BY_SIZE =
            Codec.unboundedMap(Codec.STRING.comapFlatMap(ChestProperties::parseSize, String::valueOf), Codec.LONG);

    public static final Codec<ChestProperties> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, HARD_MAX_SLOTS).optionalFieldOf("base_slots", DEFAULT.baseSlots()).forGetter(ChestProperties::baseSlots),
            Codec.intRange(1, HARD_MAX_SLOTS).optionalFieldOf("max_slots", DEFAULT.maxSlots()).forGetter(ChestProperties::maxSlots),
            CODEC_DEPTH_BY_SIZE.optionalFieldOf("depth_by_size", DEFAULT.depthBySize()).forGetter(ChestProperties::depthBySize)
    ).apply(i, ChestProperties::new));

    private static DataResult<Integer> parseSize(String key) {
        try {
            int size = Integer.parseInt(key);
            if (size < 2 || size > HARD_MAX_SIZE) {
                return DataResult.error(() -> "Size out of range [2, " + HARD_MAX_SIZE + "]: " + key);
            }
            return DataResult.success(size);
        } catch (NumberFormatException e) {
            return DataResult.error(() -> "Not a size: " + key);
        }
    }

}
