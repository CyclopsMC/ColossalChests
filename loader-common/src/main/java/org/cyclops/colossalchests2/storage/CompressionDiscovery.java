package org.cyclops.colossalchests2.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds compression families in crafting recipes, like compacting storage does: an item compresses into another if
 * a 2x2 or 3x3 recipe of only that item makes one of the other, and a recipe turns the other back into as many.
 * One-way recipes are ignored, so compression never creates or loses items.
 * @author rubensworks
 */
public final class CompressionDiscovery {

    private CompressionDiscovery() {
    }

    /**
     * A two-way conversion.
     * @param smaller The smaller form.
     * @param larger The larger form.
     * @param ratio How many of the smaller form make one of the larger.
     */
    public record Conversion(Item smaller, Item larger, int ratio) {
    }

    /**
     * @param recipes The recipes.
     * @param registries Registries, to read recipe results.
     * @return The two-way conversions in the crafting recipes.
     */
    public static List<Conversion> findConversions(RecipeManager recipes, HolderLookup.Provider registries) {
        // Larger form to the compressions that make it, and smaller form by larger form for decompressions.
        Map<Item, List<Compress>> compressions = Maps.newHashMap();
        Map<Item, Map<Item, Integer>> decompressions = Maps.newHashMap();
        for (RecipeHolder<CraftingRecipe> holder : recipes.getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(registries);
            if (result.isEmpty() || !result.getComponentsPatch().isEmpty()) {
                continue;
            }
            List<Ingredient> ingredients = recipe.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).toList();
            if (result.getCount() == 1 && isSquare(recipe, ingredients.size()) && allSame(ingredients)) {
                compressions.computeIfAbsent(result.getItem(), item -> Lists.newArrayList())
                        .add(new Compress(ingredients.get(0), ingredients.size()));
            } else if (ingredients.size() == 1 && (result.getCount() == 4 || result.getCount() == 9)) {
                for (ItemStack larger : ingredients.get(0).getItems()) {
                    if (larger.getComponentsPatch().isEmpty()) {
                        decompressions.computeIfAbsent(larger.getItem(), item -> Maps.newHashMap())
                                .put(result.getItem(), result.getCount());
                    }
                }
            }
        }
        List<Conversion> conversions = Lists.newArrayList();
        compressions.forEach((larger, candidates) -> {
            Map<Item, Integer> back = decompressions.getOrDefault(larger, Map.of());
            for (Compress compress : candidates) {
                for (ItemStack smaller : compress.ingredient().getItems()) {
                    Integer ratio = back.get(smaller.getItem());
                    if (ratio != null && ratio == compress.count() && smaller.getItem() != larger) {
                        conversions.add(new Conversion(smaller.getItem(), larger, ratio));
                    }
                }
            }
        });
        return conversions;
    }

    /**
     * Chain conversions into families. An item with more than one larger or smaller candidate is ambiguous, so its
     * conversions are left out.
     * @param conversions Two-way conversions.
     * @return The families.
     */
    public static CompressionFamilies buildFamilies(Collection<Conversion> conversions) {
        Map<Item, Conversion> up = Maps.newHashMap();
        Map<Item, Conversion> down = Maps.newHashMap();
        Set<Item> ambiguous = Sets.newHashSet();
        for (Conversion conversion : Sets.newLinkedHashSet(conversions)) {
            Conversion previousUp = up.put(conversion.smaller(), conversion);
            if (previousUp != null && !previousUp.equals(conversion)) {
                ambiguous.add(conversion.smaller());
            }
            Conversion previousDown = down.put(conversion.larger(), conversion);
            if (previousDown != null && !previousDown.equals(conversion)) {
                ambiguous.add(conversion.larger());
            }
        }
        CompressionFamilies families = new CompressionFamilies();
        for (Item bottom : up.keySet()) {
            if (down.containsKey(bottom) || ambiguous.contains(bottom)) {
                continue;
            }
            // Walk up from the smallest form.
            List<Object> chain = Lists.newArrayList();
            chain.add(bottom);
            Set<Item> seen = Sets.newHashSet(bottom);
            Item current = bottom;
            Conversion next;
            while ((next = up.get(current)) != null && !ambiguous.contains(next.larger()) && seen.add(next.larger())) {
                chain.add(0, next.ratio());
                chain.add(0, next.larger());
                current = next.larger();
            }
            if (chain.size() >= 3) {
                try {
                    families.register(CompressionFamily.of((Item) chain.get(0), chain.subList(1, chain.size()).toArray()));
                } catch (IllegalArgumentException e) {
                    // Overlaps with a family found earlier, left out.
                }
            }
        }
        return families;
    }

    private static boolean isSquare(CraftingRecipe recipe, int count) {
        if (count != 4 && count != 9) {
            return false;
        }
        if (recipe instanceof ShapedRecipe shaped) {
            return shaped.getWidth() * shaped.getHeight() == count;
        }
        return recipe instanceof ShapelessRecipe;
    }

    private static boolean allSame(List<Ingredient> ingredients) {
        Ingredient first = ingredients.get(0);
        for (Ingredient ingredient : ingredients) {
            if (!ingredient.equals(first)) {
                return false;
            }
        }
        return first.getItems().length > 0;
    }

    private record Compress(Ingredient ingredient, int count) {
    }

}
