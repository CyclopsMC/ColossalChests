package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;

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
 * @param materials Material properties by material id.
 * @param upgrades Upgrade properties by upgrade id.
 * @author rubensworks
 */
public record ChestTables(Map<ResourceLocation, MaterialProperties> materials, Map<ResourceLocation, UpgradeProperties> upgrades) {

    public static final ChestTables DEFAULT = new ChestTables(
            ImmutableMap.<ResourceLocation, MaterialProperties>builder()
                    .put(id("wood"), new MaterialProperties(1, 3, false))
                    .put(id("copper"), new MaterialProperties(2, 4, false))
                    .put(id("iron"), new MaterialProperties(3, 5, false))
                    .put(id("gold"), new MaterialProperties(4, 6, false))
                    .put(id("diamond"), new MaterialProperties(5, 7, false))
                    .put(id("obsidian"), new MaterialProperties(6, 8, true))
                    .put(id("netherite"), new MaterialProperties(7, 10, true))
                    .build(),
            ImmutableMap.<ResourceLocation, UpgradeProperties>builder()
                    .put(id("depth"), new UpgradeProperties(0, ImmutableMap.<ResourceLocation, Integer>builder()
                            .put(id("wood"), 0)
                            .put(id("copper"), 1)
                            .put(id("iron"), 2)
                            .put(id("gold"), 3)
                            .put(id("diamond"), 4)
                            .put(id("obsidian"), 5)
                            .put(id("netherite"), 6)
                            .build(), 2))
                    .put(id("slot_expansion"), new UpgradeProperties(3, Map.of(), 27))
                    .put(id("lock"), new UpgradeProperties(1, Map.of(), 1))
                    .put(id("bundling"), new UpgradeProperties(4, Map.of(), 2))
                    .put(id("void"), new UpgradeProperties(1, Map.of(), 1))
                    .put(id("compression"), new UpgradeProperties(1, Map.of(id("wood"), 0), 1))
                    .build()
    );

    /**
     * @param material A material id.
     * @return The properties of the material, or the defaults if no file defines it.
     */
    public MaterialProperties getMaterial(ResourceLocation material) {
        return materials.getOrDefault(material, MaterialProperties.DEFAULT);
    }

    /**
     * @param upgrade An upgrade id.
     * @return The properties of the upgrade, or the shipped defaults if no file defines it.
     */
    public UpgradeProperties getUpgrade(ResourceLocation upgrade) {
        UpgradeProperties properties = upgrades.get(upgrade);
        if (properties == null) {
            properties = DEFAULT.upgrades().getOrDefault(upgrade, UpgradeProperties.DISABLED);
        }
        return properties;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

}
