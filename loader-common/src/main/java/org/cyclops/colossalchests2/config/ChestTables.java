package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Data-driven values for materials and upgrades.
 * Each material and each upgrade is its own file in its owner's namespace,
 * so mods and datapacks can add entries without overwriting each other:
 * <ul>
 *     <li>data/[namespace]/colossalchests2/material/[name].json</li>
 *     <li>data/[namespace]/colossalchests2/upgrade/[name].json for upgrade values and per-material limits</li>
 * </ul>
 * Chest-wide values such as slot counts and depth by size are in {@link org.cyclops.colossalchests2.GeneralConfig}.
 * @param materials Material properties by material id, only from data files.
 * @param upgrades Upgrade properties by upgrade id, only from data files.
 * @author rubensworks
 */
public record ChestTables(Map<ResourceLocation, MaterialProperties> materials, Map<ResourceLocation, UpgradeProperties> upgrades) {

    public ChestTables {
        materials = ImmutableMap.copyOf(materials);
        upgrades = ImmutableMap.copyOf(upgrades);
    }

    /**
     * The tables before data is loaded, or sent by the server.
     */
    public static final ChestTables DEFAULT = new ChestTables(Map.of(), Map.of());

    /**
     * @param material A material id.
     * @return The properties of the material, or the defaults if no file defines it.
     */
    public MaterialProperties getMaterial(ResourceLocation material) {
        return materials.getOrDefault(material, MaterialProperties.DEFAULT);
    }

    /**
     * The upgrade's own limit for the material wins, then the material's limit for the upgrade, then the upgrade's general limit.
     * @param upgrade An upgrade id.
     * @param material A material id.
     * @return How many of the upgrade a chest of the material takes.
     */
    public int getMaxUpgradeCount(ResourceLocation upgrade, ResourceLocation material) {
        UpgradeProperties properties = getUpgrade(upgrade);
        Integer limit = properties.maxCountByMaterial().get(material);
        if (limit == null) {
            limit = getMaterial(material).upgradeLimits().get(upgrade);
        }
        return limit != null ? limit : properties.maxCount();
    }

    /**
     * @param upgrade An upgrade id.
     * @return The properties of the upgrade, or disabled if no file defines it.
     */
    public UpgradeProperties getUpgrade(ResourceLocation upgrade) {
        return upgrades.getOrDefault(upgrade, UpgradeProperties.DISABLED);
    }

}
