package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.colossalchests2.storage.DeepSlot;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Which slots the chest GUI shows, and in which order. Computed on the server, so it stays correct
 * while contents change.
 * @author rubensworks
 */
public final class ChestView {

    private ChestView() {
    }

    /**
     * @param storage The storage.
     * @param query The search query. Empty slots only show without a query.
     * @param sortMode The sort mode. When sorting, empty slots come last.
     * @param names Gives the name to search and sort on.
     * @return Slot indexes in display order.
     */
    public static int[] compute(ChestStorage storage, String query, ChestSortMode sortMode, Function<ItemStack, String> names) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        Comparator<Integer> filledFirst = Comparator.comparing(slot -> storage.getSlot(slot).isEmpty());
        Comparator<Integer> order = switch (sortMode) {
            // Slot order, with empty slots in place.
            case NONE -> Comparator.comparingInt(slot -> 0);
            case NAME -> filledFirst.thenComparing(slot -> name(storage.getSlot(slot), names));
            case COUNT -> filledFirst.thenComparing(slot -> -storage.getSlot(slot).getCount());
            case MOD -> filledFirst
                    .thenComparing(slot -> namespace(storage.getSlot(slot)))
                    .thenComparing(slot -> name(storage.getSlot(slot), names));
        };
        return IntStream.range(0, storage.getSlotCount())
                .filter(slot -> matches(storage.getSlot(slot), needle, names))
                .boxed()
                .sorted(order.thenComparingInt(slot -> slot))
                .mapToInt(Integer::intValue)
                .toArray();
    }

    private static boolean matches(DeepSlot slot, String needle, Function<ItemStack, String> names) {
        if (needle.isEmpty()) {
            return true;
        }
        return !slot.isEmpty() && (name(slot, names).contains(needle) || id(slot).contains(needle));
    }

    private static String name(DeepSlot slot, Function<ItemStack, String> names) {
        return slot.isEmpty() ? "" : names.apply(slot.getPrototype()).toLowerCase(Locale.ROOT);
    }

    private static String namespace(DeepSlot slot) {
        return slot.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(slot.getPrototype().getItem()).getNamespace();
    }

    private static String id(DeepSlot slot) {
        return slot.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(slot.getPrototype().getItem()).toString();
    }

}
