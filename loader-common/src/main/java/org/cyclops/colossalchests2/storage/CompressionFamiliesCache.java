package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The compression families found in a level's recipes, rebuilt when the recipes change.
 * Works on both sides, as 1.21.1 clients receive all recipes. Kept per recipe manager, as the integrated server and
 * its client each have one.
 * @author rubensworks
 */
public final class CompressionFamiliesCache {

    private static final Map<RecipeManager, Entry> CACHE = new WeakHashMap<>();

    private CompressionFamiliesCache() {
    }

    /**
     * @param level A level.
     * @return The compression families of its recipes.
     */
    public static synchronized CompressionFamilies get(Level level) {
        RecipeManager manager = level.getRecipeManager();
        // A client reload replaces the manager's recipe map rather than the manager.
        Collection<?> recipes = manager.getRecipes();
        Entry entry = CACHE.get(manager);
        if (entry == null || entry.recipes() != recipes) {
            entry = new Entry(recipes, CompressionDiscovery.buildFamilies(CompressionDiscovery.findConversions(manager, level.registryAccess())));
            CACHE.put(manager, entry);
        }
        return entry.families();
    }

    private record Entry(Collection<?> recipes, CompressionFamilies families) {
    }

}
