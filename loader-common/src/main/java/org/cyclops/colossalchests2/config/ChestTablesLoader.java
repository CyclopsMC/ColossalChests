package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Holds the active {@link ChestTables}.
 * Filled from datapacks by {@link ChestTablesReloadListener}, defaults until the first load.
 * @author rubensworks
 */
public class ChestTablesLoader {

    public static final String DIRECTORY = "colossalchests2";
    public static final String DIRECTORY_MATERIAL = DIRECTORY + "/material";
    public static final String DIRECTORY_UPGRADE = DIRECTORY + "/upgrade";

    private static ChestTables current = ChestTables.DEFAULT;

    public static ChestTables get() {
        return current;
    }

    public static void set(ChestTables tables) {
        current = tables;
    }

    /**
     * Build the tables from the loaded data files, throwing on invalid files.
     * @param files Files below {@link #DIRECTORY}, by id with a material/ or upgrade/ path prefix.
     * @return The tables.
     */
    public static ChestTables fromJson(Map<ResourceLocation, JsonElement> files) {
        return fromJson(files, error -> {
            throw error;
        });
    }

    /**
     * Build the tables from the loaded data files.
     * @param files Files below {@link #DIRECTORY}, by id with a material/ or upgrade/ path prefix.
     * @param onError Called for each invalid file, which is then skipped.
     * @return The tables.
     */
    public static ChestTables fromJson(Map<ResourceLocation, JsonElement> files, Consumer<IllegalArgumentException> onError) {
        ImmutableMap.Builder<ResourceLocation, MaterialProperties> materials = ImmutableMap.builder();
        ImmutableMap.Builder<ResourceLocation, UpgradeProperties> upgrades = ImmutableMap.builder();
        files.forEach((id, json) -> {
            try {
                String path = id.getPath();
                if (path.startsWith("material/")) {
                    ResourceLocation material = id.withPath(path.substring("material/".length()));
                    materials.put(material, parse(MaterialProperties.CODEC, json, "material " + material));
                } else if (path.startsWith("upgrade/")) {
                    ResourceLocation upgrade = id.withPath(path.substring("upgrade/".length()));
                    upgrades.put(upgrade, parse(UpgradeProperties.CODEC, json, "upgrade " + upgrade));
                }
            } catch (IllegalArgumentException e) {
                onError.accept(e);
            }
        });
        return new ChestTables(materials.build(), upgrades.build());
    }

    public static <T> T parse(Codec<T> codec, JsonElement json, String source) {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow(error -> new IllegalArgumentException("Invalid " + source + ": " + error));
    }

}
