package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.DeepSlot;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Which slots match the search query of the chest GUI. Matching slots are highlighted, slots never move.
 * The syntax is that of JEI, Refined Storage and Integrated Terminals:
 * terms separated by spaces must all match, alternatives within a term are separated by {@code |},
 * and an alternative matches the item name or id by default, the mod id with {@code @},
 * a tooltip line with {@code #}, or a tag with {@code $}.
 * @author rubensworks
 */
public final class ChestSearch {

    private ChestSearch() {
    }

    /**
     * @param slot A slot.
     * @param query The search query, ignoring case.
     * @param names Gives the display name of an item.
     * @param tooltips Gives the tooltip lines of an item.
     * @return If the slot matches. Everything matches an empty query, empty slots match nothing else.
     */
    public static boolean matches(DeepSlot slot, String query, Function<ItemStack, String> names,
                                  Function<ItemStack, List<String>> tooltips) {
        String[] terms = query.toLowerCase(Locale.ROOT).trim().split(" +");
        if (terms[0].isEmpty()) {
            return true;
        }
        if (slot.isEmpty()) {
            return false;
        }
        ItemStack stack = slot.getPrototype();
        return Arrays.stream(terms).allMatch(term -> Arrays.stream(term.split("\\|"))
                .anyMatch(alternative -> matchesAlternative(stack, alternative, names, tooltips)));
    }

    private static boolean matchesAlternative(ItemStack stack, String alternative, Function<ItemStack, String> names,
                                              Function<ItemStack, List<String>> tooltips) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (alternative.isEmpty()) {
            return false;
        }
        String needle = alternative.substring(1);
        return switch (alternative.charAt(0)) {
            case '@' -> id.getNamespace().contains(needle);
            case '#' -> tooltips.apply(stack).stream().anyMatch(line -> line.toLowerCase(Locale.ROOT).contains(needle));
            case '$' -> stack.getTags().anyMatch(tag -> tag.location().toString().contains(needle));
            default -> names.apply(stack).toLowerCase(Locale.ROOT).contains(alternative) || id.toString().contains(alternative);
        };
    }

}
