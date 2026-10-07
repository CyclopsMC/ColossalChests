package org.cyclops.colossalchests2.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.colossalchests2.upgrade.ChestUpgrade;
import org.cyclops.colossalchests2.upgrade.ChestUpgrades;
import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        for (ResourceLocation id : ChestTables.DEFAULT.upgrades().keySet()) {
            files.put(upgrade(id), read(ChestTablesLoader.DIRECTORY_UPGRADE + "/" + id.getPath() + ".json"));
        }
        assertEquals(ChestTables.DEFAULT.upgrades(), ChestTablesLoader.fromJson(files).upgrades());
    }

    @Test
    public void testShippedMaterials() throws Exception {
        Map<ResourceLocation, JsonElement> files = Maps.newHashMap();
        for (ChestMaterial material : ChestMaterial.BUILT_IN) {
            files.put(material(material.id()), read(ChestTablesLoader.DIRECTORY_MATERIAL + "/" + material.getName() + ".json"));
        }
        ChestTables tables = ChestTablesLoader.fromJson(files);
        assertEquals(new MaterialProperties(1, 3, false), tables.getMaterial(ChestMaterial.WOOD.id()));
        assertEquals(new MaterialProperties(7, 10, true, Map.of(), Optional.of(ChestMaterial.OBSIDIAN.id())),
                tables.getMaterial(ChestMaterial.NETHERITE.id()));
        // Each material comes after the previous one.
        for (int i = 1; i < ChestMaterial.BUILT_IN.size(); i++) {
            assertEquals(Optional.of(ChestMaterial.BUILT_IN.get(i - 1).id()), tables.getMaterial(ChestMaterial.BUILT_IN.get(i).id()).after());
        }
    }

    @Test
    public void testMaterialAfter() {
        ChestTables tables = ChestTablesLoader.fromJson(Map.of(material(TIN), JsonParser.parseString("{\"after\": \"colossalchests2:copper\"}")));
        assertEquals(Optional.of(ChestMaterial.COPPER.id()), tables.getMaterial(TIN).after());
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

    @Test
    public void testRegisteredUpgradeUsesItsDefaults() {
        UpgradeProperties defaults = new UpgradeProperties(2, Map.of(), 5);
        ChestUpgrade upgrade = new ChestUpgrade(ResourceLocation.fromNamespaceAndPath("othermod", "tables_test"), defaults);
        ChestUpgrades.register(upgrade);
        assertEquals(defaults, ChestTables.DEFAULT.getUpgrade(upgrade.getId()));
        assertEquals(UpgradeProperties.DISABLED, ChestTables.DEFAULT.getUpgrade(ResourceLocation.fromNamespaceAndPath("othermod", "unknown")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterUpgradeDuplicate() {
        ChestUpgrades.register(ChestUpgrades.DEPTH);
    }

}
