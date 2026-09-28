package org.cyclops.colossalchests2.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestChestTables {

    private static JsonElement read(String path) throws Exception {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(
                TestChestTables.class.getResourceAsStream("/data/colossalchests2/" + path), path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }

    @Test
    public void testShippedMaterialsMatchDefaults() throws Exception {
        assertEquals(ChestTables.DEFAULT.materials(), ChestTablesLoader.parse(ChestTables.CODEC_MATERIALS, read(ChestTables.PATH_MATERIALS)));
    }

    @Test
    public void testShippedSizeDepthMatchDefaults() throws Exception {
        assertEquals(ChestTables.DEFAULT.depthBySize(), ChestTablesLoader.parse(ChestTables.CODEC_SIZE_DEPTH, read(ChestTables.PATH_SIZE_DEPTH)));
    }

    @Test
    public void testShippedUpgradesMatchDefaults() throws Exception {
        assertEquals(ChestTables.DEFAULT.upgrades(), ChestTablesLoader.parse(UpgradeValues.CODEC, read(ChestTables.PATH_UPGRADES)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeOutOfRangeRejected() {
        ChestTablesLoader.parse(ChestTables.CODEC_SIZE_DEPTH, JsonParser.parseString("{\"depth_by_size\": {\"11\": 4}}"));
    }

}
