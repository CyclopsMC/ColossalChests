package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Map;

/**
 * The data-driven tables of D-2 and D-5: materials, depth by size and upgrade values.
 * Shipped as JSON in data/colossalchests2/chest_tables/, overridable by datapacks.
 * @param materials Material properties by material name.
 * @param depthBySize Stacks per slot by structure size.
 * @param upgrades Upgrade values.
 * @author rubensworks
 */
public record ChestTables(Map<String, MaterialProperties> materials, Map<Integer, Long> depthBySize, UpgradeValues upgrades) {

    public static final int HARD_MAX_SIZE = 10;

    public static final String PATH_MATERIALS = "chest_tables/materials.json";
    public static final String PATH_SIZE_DEPTH = "chest_tables/size_depth.json";
    public static final String PATH_UPGRADES = "chest_tables/upgrades.json";

    public static final Codec<Map<String, MaterialProperties>> CODEC_MATERIALS =
            Codec.unboundedMap(Codec.STRING, MaterialProperties.CODEC).fieldOf("materials").codec();
    public static final Codec<Map<Integer, Long>> CODEC_SIZE_DEPTH =
            Codec.unboundedMap(Codec.STRING.comapFlatMap(ChestTables::parseSize, String::valueOf), Codec.LONG)
                    .fieldOf("depth_by_size").codec();

    public static final ChestTables DEFAULT = new ChestTables(
            ImmutableMap.<String, MaterialProperties>builder()
                    .put("wood", new MaterialProperties(1, 0, 3, false))
                    .put("copper", new MaterialProperties(2, 1, 4, false))
                    .put("iron", new MaterialProperties(3, 2, 5, false))
                    .put("gold", new MaterialProperties(4, 3, 6, false))
                    .put("diamond", new MaterialProperties(5, 4, 7, false))
                    .put("obsidian", new MaterialProperties(6, 5, 8, true))
                    .put("netherite", new MaterialProperties(7, 6, 10, true))
                    .build(),
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
                    .build(),
            UpgradeValues.DEFAULT
    );

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
