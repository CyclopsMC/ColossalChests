package org.cyclops.colossalchests2.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

/**
 * Holds the active {@link ChestTables}.
 * Stub: parsing works, wiring into datapack reloading comes with the multiblock phase.
 * @author rubensworks
 */
public class ChestTablesLoader {

    private static ChestTables current = ChestTables.DEFAULT;

    public static ChestTables get() {
        return current;
    }

    public static void set(ChestTables tables) {
        current = tables;
    }

    public static <T> T parse(Codec<T> codec, JsonElement json) {
        return codec.parse(JsonOps.INSTANCE, json).getOrThrow(IllegalArgumentException::new);
    }

}
