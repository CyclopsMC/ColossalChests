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
import java.util.Optional;

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

    private static ChestTables readShipped() throws Exception {
        Map<ResourceLocation, JsonElement> materials = Maps.newHashMap();
        for (ResourceLocation id : ChestTables.DEFAULT.materials().keySet()) {
            materials.put(id, read(ChestTablesLoader.DIRECTORY_MATERIAL + "/" + id.getPath() + ".json"));
        }
        return ChestTablesLoader.fromJson(materials, Optional.of(read(ChestTablesLoader.PATH_CHEST)));
    }

    @Test
    public void testShippedFilesMatchDefaults() throws Exception {
        assertEquals(ChestTables.DEFAULT, readShipped());
    }

    @Test
    public void testMissingChestFileUsesDefaults() {
        assertEquals(ChestProperties.DEFAULT, ChestTablesLoader.fromJson(Map.of(), Optional.empty()).chest());
    }

    @Test
    public void testMissingFieldsUseDefaults() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("othermod", "tin");
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(id, JsonParser.parseString("{\"max_size\": 4}")),
                Optional.of(JsonParser.parseString("{\"base_slots\": 9}")));
        assertEquals(new MaterialProperties(MaterialProperties.DEFAULT.upgradeSlots(), 4, false), tables.getMaterial(id));
        assertEquals(9, tables.chest().baseSlots());
        assertEquals(ChestProperties.DEFAULT.depthBySize(), tables.chest().depthBySize());
    }

    @Test
    public void testUnknownFieldsIgnored() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("othermod", "tin");
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(id, JsonParser.parseString("{\"max_size\": 4, \"future_field\": 1}")), Optional.empty());
        assertEquals(4, tables.getMaterial(id).maxSize());
    }

    @Test
    public void testUnknownMaterialUsesDefaults() {
        assertEquals(MaterialProperties.DEFAULT, ChestTables.DEFAULT.getMaterial(ResourceLocation.fromNamespaceAndPath("othermod", "tin")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSizeOutOfRangeRejected() {
        ChestTablesLoader.fromJson(Map.of(), Optional.of(JsonParser.parseString("{\"depth_by_size\": {\"11\": 4}}")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaterialOutOfRangeRejected() {
        ChestTablesLoader.fromJson(Map.of(ResourceLocation.fromNamespaceAndPath("othermod", "tin"), JsonParser.parseString("{\"max_size\": 11}")), Optional.empty());
    }

}
