package org.cyclops.colossalchests2.capability;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * What automation may do with a chest through one wall.
 * @param mode The directions items may move in.
 * @param filter The types that may move, any type when empty.
 * @param extractionForm The compression form to extract in, or null for the slot's own form.
 * @param voidFull If inserts of a type the chest holds are destroyed when they do not fit.
 * @author rubensworks
 */
public record WallAccess(Mode mode, List<ItemStack> filter, @Nullable Item extractionForm, boolean voidFull) {

    /**
     * Anything goes, like the core itself.
     */
    public static final WallAccess OPEN = new WallAccess(Mode.BOTH, List.of(), null, false);

    public WallAccess {
        filter = filter.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).toList();
    }

    /**
     * @param storage The storage, for its compression families.
     * @param type An item type.
     * @return If the type passes the filter. A filter entry also lets the other forms of its compression family pass.
     */
    public boolean allows(ChestStorage storage, ItemStack type) {
        if (filter.isEmpty()) {
            return true;
        }
        ItemStack stored = storage.getStoredType(type);
        for (ItemStack entry : filter) {
            if (ItemStack.isSameItemSameComponents(entry, type) || ItemStack.isSameItemSameComponents(storage.getStoredType(entry), stored)) {
                return true;
            }
        }
        return false;
    }

    public boolean canInsert(ChestStorage storage, ItemStack type) {
        return mode.canInsert() && allows(storage, type);
    }

    public boolean canExtract(ChestStorage storage, ItemStack type) {
        return mode.canExtract() && allows(storage, type);
    }

    public enum Mode {
        BOTH(true, true),
        INPUT(true, false),
        OUTPUT(false, true);

        private final boolean insert;
        private final boolean extract;

        Mode(boolean insert, boolean extract) {
            this.insert = insert;
            this.extract = extract;
        }

        public boolean canInsert() {
            return insert;
        }

        public boolean canExtract() {
            return extract;
        }

        public Mode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public String getTranslationKey() {
            return "gui.colossalchests2.wall.mode." + name().toLowerCase(Locale.ROOT);
        }
    }
}
