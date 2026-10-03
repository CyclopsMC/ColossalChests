package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * Holds the active {@link ChestTables}.
 * Stub: parsing works, wiring into datapack reloading comes with the multiblock phase.
 * @author rubensworks
 */
public class ChestTablesLoader {

    public static final String DIRECTORY_MATERIAL = "colossalchests2/material";
    public static final String PATH_CHEST = "colossalchests2/chest.json";

    private static ChestTables current = ChestTables.DEFAULT;

    public static ChestTables get() {
        return current;
    }

    public static void set(ChestTables tables) {
        current = tables;
    }

    /**
     * Build the tables from the loaded data files.
     * @param materials Material files by material id.
     * @param chest The chest-wide file, if present.
     * @return The tables.
     */
    public static ChestTables fromJson(Map<ResourceLocation, JsonElement> materials, Optional<JsonElement> chest) {
        ImmutableMap.Builder<ResourceLocation, MaterialProperties> builder = ImmutableMap.builder();
        materials.forEach((id, json) -> builder.put(id, parse(MaterialProperties.CODEC, json, "material " + id)));
        return new ChestTables(builder.build(), chest.map(json -> parse(ChestProperties.CODEC, json, PATH_CHEST)).orElse(ChestProperties.DEFAULT));
    }

    public static <T> T parse(Codec<T> codec, JsonElement json, String source) {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow(error -> new IllegalArgumentException("Invalid " + source + ": " + error));
    }

}
