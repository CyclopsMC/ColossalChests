package org.cyclops.colossalchests2.block;

import java.util.Locale;

/**
 * The kinds of functional walls. They fit chests of any material.
 * @author rubensworks
 */
public enum WallType {
    /**
     * Item I/O on its faces.
     */
    INTERFACE,
    /**
     * Item I/O limited by its filter, mode and extraction form, configured in its own GUI.
     */
    FILTERED_INTERFACE,
    /**
     * Item I/O where inserts of held types that do not fit are destroyed.
     */
    VOID;

    public static final WallType[] VALUES = values();

    /**
     * @return The name used in registry ids, such as chest_wall_interface.
     */
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String getRegistryName() {
        return "chest_wall_" + getName();
    }
}
