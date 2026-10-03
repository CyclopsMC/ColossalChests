package org.cyclops.colossalchests2.storage;

import java.util.List;

/**
 * The outcome of validating a resize of a {@link ChestStorage}.
 * @param offendingSlots Slots whose contents would not fit, empty if the resize is allowed.
 * @author rubensworks
 */
public record ResizeResult(List<Integer> offendingSlots) {

    public static final ResizeResult OK = new ResizeResult(List.of());

    public ResizeResult {
        offendingSlots = List.copyOf(offendingSlots);
    }

    public static ResizeResult of(List<Integer> offendingSlots) {
        return offendingSlots.isEmpty() ? OK : new ResizeResult(offendingSlots);
    }

    public boolean isOk() {
        return offendingSlots.isEmpty();
    }

}
