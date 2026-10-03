package org.cyclops.colossalchests2.config;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Level;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.Reference;

import java.util.Map;

/**
 * Loads the material data files from datapacks into {@link ChestTablesLoader}.
 * Invalid files are logged and skipped, so their materials fall back to defaults.
 * @author rubensworks
 */
public class ChestTablesReloadListener extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_tables");

    public ChestTablesReloadListener() {
        super(new Gson(), ChestTablesLoader.DIRECTORY_MATERIAL);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        ImmutableMap.Builder<ResourceLocation, MaterialProperties> materials = ImmutableMap.builder();
        files.forEach((id, json) -> {
            try {
                materials.put(id, ChestTablesLoader.parse(MaterialProperties.CODEC, json, "material " + id));
            } catch (IllegalArgumentException e) {
                ColossalChestsInstance.MOD.log(Level.ERROR, e.getMessage());
            }
        });
        ChestTablesLoader.set(new ChestTables(materials.build()));
    }

}
