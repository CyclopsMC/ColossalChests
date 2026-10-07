package org.cyclops.colossalchests2.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.*;

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

    private static ResourceLocation material(ResourceLocation id) {
        return id.withPrefix("material/");
    }

    private static ResourceLocation upgrade(ResourceLocation id) {
        return id.withPrefix("upgrade/");
    }

    @Test
    public void testShippedFilesMatchDefaults() throws Exception {
        Map<ResourceLocation, JsonElement> files = Maps.newHashMap();
        for (ResourceLocation id : ChestTables.DEFAULT.materials().keySet()) {
            files.put(material(id), read(ChestTablesLoader.DIRECTORY_MATERIAL + "/" + id.getPath() + ".json"));
        }
        for (ResourceLocation id : ChestTables.DEFAULT.upgrades().keySet()) {
            files.put(upgrade(id), read(ChestTablesLoader.DIRECTORY_UPGRADE + "/" + id.getPath() + ".json"));
        }
        assertEquals(ChestTables.DEFAULT, ChestTablesLoader.fromJson(files));
    }

    @Test
    public void testMissingFieldsUseDefaults() {
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(material(TIN), JsonParser.parseString("{\"max_size\": 4}")));
        assertEquals(new MaterialProperties(MaterialProperties.DEFAULT.upgradeSlots(), 4, false), tables.getMaterial(TIN));
    }

    @Test
    public void testMaterialUpgradeLimits() {
        ResourceLocation depth = ChestUpgrades.DEPTH.getId();
        ResourceLocation copper = ResourceLocation.fromNamespaceAndPath("othermod", "copper");
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(
                upgrade(depth), JsonParser.parseString("{\"max_count\": 2, \"max_count_by_material\": {\"othermod:copper\": 4}}"),
                material(TIN), JsonParser.parseString("{\"upgrade_limits\": {\"colossalchests2:depth\": 5}}"),
                material(copper), JsonParser.parseString("{\"upgrade_limits\": {\"colossalchests2:depth\": 5}}")));
        assertEquals(5, tables.getMaxUpgradeCount(depth, TIN));
        // The upgrade's own limit for a material wins.
        assertEquals(4, tables.getMaxUpgradeCount(depth, copper));
        assertEquals(2, tables.getMaxUpgradeCount(depth, ResourceLocation.fromNamespaceAndPath("othermod", "lead")));
    }

    @Test
    public void testRegisteredMaterialUsesItsDefaults() {
        MaterialProperties defaults = new MaterialProperties(3, 6, true, Map.of(ChestUpgrades.DEPTH.getId(), 2));
        ChestMaterial material = new ChestMaterial(ResourceLocation.fromNamespaceAndPath("othermod", "tables_test"),
                SoundType.METAL, 1, true, 1, ChestMaterial.IRON.id(), defaults);
        ChestMaterial.register(material);
        assertEquals(defaults, ChestTables.DEFAULT.getMaterial(material.id()));
        assertEquals(2, ChestTables.DEFAULT.getMaxUpgradeCount(ChestUpgrades.DEPTH.getId(), material.id()));
        // A data file still wins.
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(material(material.id()), JsonParser.parseString("{\"max_size\": 4}")));
        assertEquals(4, tables.getMaterial(material.id()).maxSize());
    }

    @Test
    public void testUnknownFieldsIgnored() {
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(material(TIN), JsonParser.parseString("{\"max_size\": 4, \"future_field\": 1}")));
        assertEquals(4, tables.getMaterial(TIN).maxSize());
    }

    @Test
    public void testUnknownMaterialUsesDefaults() {
        assertEquals(MaterialProperties.DEFAULT, ChestTables.DEFAULT.getMaterial(TIN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMaterialOutOfRangeRejected() {
        ChestTablesLoader.fromJson(Map.of(material(TIN), JsonParser.parseString("{\"max_size\": 11}")));
    }

    @Test
    public void testUpgradeLimitsByMaterial() {
        ResourceLocation depth = ChestUpgrades.DEPTH.getId();
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(upgrade(depth),
                JsonParser.parseString("{\"max_count\": 2, \"max_count_by_material\": {\"othermod:tin\": 5}, \"value\": 4}")));
        assertEquals(5, tables.getUpgrade(depth).getMaxCount(TIN));
        assertEquals(2, tables.getUpgrade(depth).getMaxCount(ResourceLocation.fromNamespaceAndPath("othermod", "lead")));
        assertEquals(4, tables.getUpgrade(depth).value());
    }

    @Test
    public void testMissingUpgradeUsesShippedDefaults() {
        ChestTables tables = ChestTablesLoader.fromJson(Map.of());
        assertEquals(ChestTables.DEFAULT.getUpgrade(ChestUpgrades.LOCK.getId()), tables.getUpgrade(ChestUpgrades.LOCK.getId()));
        assertEquals(UpgradeProperties.DISABLED, tables.getUpgrade(ResourceLocation.fromNamespaceAndPath("othermod", "unknown")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpgradeValueBelowOneRejected() {
        ChestTablesLoader.fromJson(Map.of(upgrade(ChestUpgrades.DEPTH.getId()), JsonParser.parseString("{\"value\": 0}")));
    }

    @Test
    public void testInvalidFilesSkippedWithHandler() {
        List<IllegalArgumentException> errors = Lists.newArrayList();
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(material(TIN), JsonParser.parseString("{\"max_size\": 11}")), errors::add);
        assertEquals(1, errors.size());
        assertEquals(MaterialProperties.DEFAULT, tables.getMaterial(TIN));
    }

}
