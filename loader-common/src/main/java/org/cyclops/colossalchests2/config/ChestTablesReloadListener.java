package org.cyclops.colossalchests2.config;

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
 * Loads the material and upgrade data files from datapacks into {@link ChestTablesLoader}.
 * Invalid files are logged and skipped, so their entries fall back to defaults.
 * @author rubensworks
 */
public class ChestTablesReloadListener extends SimpleJsonResourceReloadListener {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_tables");

    public ChestTablesReloadListener() {
        super(new Gson(), ChestTablesLoader.DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        ChestTablesLoader.set(ChestTablesLoader.fromJson(files, error -> ColossalChestsInstance.MOD.log(Level.ERROR, error.getMessage())));
    }

}
