package org.cyclops.colossalchests2.storage;

import com.google.common.collect.Maps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Lookup of {@link CompressionFamily}s by item.
 * @author rubensworks
 */
public class CompressionFamilies {

    public static final CompressionFamilies EMPTY = new CompressionFamilies();

    private final Map<Item, CompressionFamily> byItem = Maps.newIdentityHashMap();
    private final Set<CompressionFamily> families = new LinkedHashSet<>();

    /**
     * Register a family. An item can only belong to one family.
     * @param family A family.
     */
    public void register(CompressionFamily family) {
        for (CompressionFamily.Form form : family.forms()) {
            if (byItem.containsKey(form.item())) {
                throw new IllegalArgumentException("Item " + form.item() + " is already in a compression family");
            }
        }
        for (CompressionFamily.Form form : family.forms()) {
            byItem.put(form.item(), family);
        }
        families.add(family);
    }

    public Optional<CompressionFamily> find(Item item) {
        return Optional.ofNullable(byItem.get(item));
    }

    /**
     * @param stack An item stack.
     * @return The family of a plain stack.
     */
    public Optional<CompressionFamily> find(ItemStack stack) {
        return stack.isEmpty() || !stack.getComponentsPatch().isEmpty() ? Optional.empty() : find(stack.getItem());
    }

    public Collection<CompressionFamily> getFamilies() {
        return Collections.unmodifiableSet(families);
    }

    public void clear() {
        byItem.clear();
        families.clear();
    }

}
