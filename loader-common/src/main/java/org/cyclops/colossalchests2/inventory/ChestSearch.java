package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.DeepSlot;

import java.util.Locale;
import java.util.function.Function;

/**
 * Which slots match the search query of the chest GUI. Matching slots are highlighted, slots never move.
 * @author rubensworks
 */
public final class ChestSearch {

    private ChestSearch() {
    }

    /**
     * @param slot A slot.
     * @param query The search query, ignoring case and surrounding spaces.
     * @param names Gives the display name of an item.
     * @return If the slot matches. Everything matches an empty query, empty slots match nothing else.
     */
    public static boolean matches(DeepSlot slot, String query, Function<ItemStack, String> names) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return true;
        }
        if (slot.isEmpty()) {
            return false;
        }
        String name = names.apply(slot.getPrototype()).toLowerCase(Locale.ROOT);
        String id = BuiltInRegistries.ITEM.getKey(slot.getPrototype().getItem()).toString();
        return name.contains(needle) || id.contains(needle);
    }

}
