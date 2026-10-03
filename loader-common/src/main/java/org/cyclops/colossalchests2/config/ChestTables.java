package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import org.cyclops.colossalchests2.Reference;

import java.util.Map;

/**
 * The data-driven tables of D-2 and D-5.
 * Each material and (later) each upgrade is its own file in its owner's namespace,
 * so mods and datapacks can add entries without overwriting each other:
 * <ul>
 *     <li>data/[namespace]/colossalchests2/material/[name].json</li>
 *     <li>data/[namespace]/colossalchests2/upgrade/[name].json (with the upgrade phase)</li>
 *     <li>data/colossalchests2/colossalchests2/chest.json for chest-wide values</li>
 * </ul>
 * @param materials Material properties by material id.
 * @param chest Chest-wide values.
 * @author rubensworks
 */
public record ChestTables(Map<ResourceLocation, MaterialProperties> materials, ChestProperties chest) {

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
            ChestProperties.DEFAULT
    );

    /**
     * @param material A material id.
     * @return The properties of the material, or the defaults if no file defines it.
     */
    public MaterialProperties getMaterial(ResourceLocation material) {
        return materials.getOrDefault(material, MaterialProperties.DEFAULT);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, path);
    }

}
