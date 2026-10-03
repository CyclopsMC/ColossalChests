package org.cyclops.colossalchests2.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * An ordered set of item forms that convert into each other, such as block, ingot and nugget.
 * Counts are converted through base units: the amount of the smallest form one item represents.
 * @param forms The forms, from largest to smallest. The smallest form has 1 base unit.
 * @author rubensworks
 */
public record CompressionFamily(List<Form> forms) {

    public CompressionFamily {
        forms = List.copyOf(forms);
        if (forms.size() < 2) {
            throw new IllegalArgumentException("A compression family needs at least two forms");
        }
        if (forms.get(forms.size() - 1).baseUnits() != 1) {
            throw new IllegalArgumentException("The smallest form must be worth 1 base unit");
        }
        for (int i = 1; i < forms.size(); i++) {
            Form larger = forms.get(i - 1);
            Form smaller = forms.get(i);
            if (larger.baseUnits() <= smaller.baseUnits() || larger.baseUnits() % smaller.baseUnits() != 0) {
                throw new IllegalArgumentException("Each form must be a whole multiple of the next smaller form");
            }
            for (int j = 0; j < i; j++) {
                if (forms.get(j).item() == smaller.item()) {
                    throw new IllegalArgumentException("Duplicate form " + smaller.item());
                }
            }
        }
    }

    /**
     * Create a family from the largest form down, with the ratio between neighbouring forms.
     * For example (block, 9, ingot, 9, nugget).
     * @param largest The largest form.
     * @param rest Alternating ratio (Integer) and next smaller form (Item).
     * @return The family.
     */
    public static CompressionFamily of(Item largest, Object... rest) {
        if (rest.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating ratios and items");
        }
        int n = rest.length / 2 + 1;
        Item[] items = new Item[n];
        long[] ratios = new long[n - 1];
        items[0] = largest;
        for (int i = 0; i < rest.length; i += 2) {
            ratios[i / 2] = ((Number) rest[i]).longValue();
            items[i / 2 + 1] = (Item) rest[i + 1];
        }
        Form[] forms = new Form[n];
        long units = 1;
        for (int i = n - 1; i >= 0; i--) {
            forms[i] = new Form(items[i], units);
            if (i > 0) {
                units = CapacityProfile.saturatedMultiply(units, ratios[i - 1]);
            }
        }
        return new CompressionFamily(List.of(forms));
    }

    public int size() {
        return forms.size();
    }

    public Form largest() {
        return forms.get(0);
    }

    public Form smallest() {
        return forms.get(forms.size() - 1);
    }

    public Form get(int form) {
        return forms.get(form);
    }

    /**
     * @param item An item.
     * @return The form index of the item, or -1.
     */
    public int indexOf(Item item) {
        for (int i = 0; i < forms.size(); i++) {
            if (forms.get(i).item() == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Only plain stacks without extra components can be compressed.
     * @param stack An item stack.
     * @return The form index of the stack, or -1.
     */
    public int indexOf(ItemStack stack) {
        if (stack.isEmpty() || !stack.getComponentsPatch().isEmpty()) {
            return -1;
        }
        return indexOf(stack.getItem());
    }

    /**
     * @param form A form index.
     * @param count An amount of that form.
     * @return The amount in base units, saturated at Long.MAX_VALUE.
     */
    public long toBaseUnits(int form, long count) {
        return CapacityProfile.saturatedMultiply(count, forms.get(form).baseUnits());
    }

    /**
     * @param form A form index.
     * @param baseUnits An amount of base units.
     * @return How many whole items of the form the base units make.
     */
    public long fromBaseUnits(int form, long baseUnits) {
        return baseUnits / forms.get(form).baseUnits();
    }

    /**
     * @param form A form index.
     * @param baseUnits An amount of base units.
     * @return The base units left over after converting to whole items of the form.
     */
    public long remainderBaseUnits(int form, long baseUnits) {
        return baseUnits % forms.get(form).baseUnits();
    }

    /**
     * Convert between two forms, rounding down.
     * @param fromForm The source form index.
     * @param count The amount in the source form.
     * @param toForm The target form index.
     * @return The whole amount in the target form.
     */
    public long convert(int fromForm, long count, int toForm) {
        return fromBaseUnits(toForm, toBaseUnits(fromForm, count));
    }

    /**
     * @param item The item of the form.
     * @param baseUnits How many of the smallest form one item of this form is worth.
     */
    public record Form(Item item, long baseUnits) {
        public Form {
            if (baseUnits < 1) {
                throw new IllegalArgumentException("Base units must be positive");
            }
        }
    }
}
