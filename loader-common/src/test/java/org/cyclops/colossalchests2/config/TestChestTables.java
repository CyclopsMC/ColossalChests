package org.cyclops.colossalchests2.config;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author rubensworks
 */
public class TestChestTables {

    private static final ResourceLocation TIN = ResourceLocation.fromNamespaceAndPath("othermod", "tin");

    private static JsonElement read(String path) throws Exception {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(
                TestChestTables.class.getResourceAsStream("/data/colossalchests2/" + path), path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        }
    }

    @Test
    public void testShippedFilesMatchDefaults() throws Exception {
        Map<ResourceLocation, JsonElement> materials = Maps.newHashMap();
        for (ResourceLocation id : ChestTables.DEFAULT.materials().keySet()) {
            materials.put(id, read(ChestTablesLoader.DIRECTORY_MATERIAL + "/" + id.getPath() + ".json"));
        }
        assertEquals(ChestTables.DEFAULT, ChestTablesLoader.fromJson(materials));
    }

    @Test
    public void testMissingFieldsUseDefaults() {
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(TIN, JsonParser.parseString("{\"max_size\": 4}")));
        assertEquals(new MaterialProperties(MaterialProperties.DEFAULT.upgradeSlots(), 4, false), tables.getMaterial(TIN));
    }

    @Test
    public void testUnknownFieldsIgnored() {
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(TIN, JsonParser.parseString("{\"max_size\": 4, \"future_field\": 1}")));
        assertEquals(4, tables.getMaterial(TIN).maxSize());
    }

    @Test
    public void testUnknownMaterialUsesDefaults() {
        assertEquals(MaterialProperties.DEFAULT, ChestTables.DEFAULT.getMaterial(TIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaterialOutOfRangeRejected() {
        ChestTablesLoader.fromJson(Map.of(TIN, JsonParser.parseString("{\"max_size\": 11}")));
    }

}
