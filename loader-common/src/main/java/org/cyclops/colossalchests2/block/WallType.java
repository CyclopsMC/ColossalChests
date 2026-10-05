package org.cyclops.colossalchests2.block;

import java.util.Locale;

/**
 * The kinds of functional walls. They fit chests of any material.
 * @author rubensworks
 */
public enum WallType {
    /**
     * Item I/O on its faces, optionally limited by a filter and direction set in its own GUI.
     */
    INTERFACE,
    /**
     * Item I/O where inserts of held types that do not fit are destroyed.
     */
    VOID,
    /**
     * Shows one item type and its count on its faces, and lets players take and insert it by clicking.
     */
    DISPLAY,
    /**
     * Pulls dropped items around it into the chest.
     */
    MAGNET;

    public static final WallType[] VALUES = values();

    /**
     * @return The name used in registry ids, such as chest_wall_interface.
     */
    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * @return If automation can move items through this wall.
     */
    public boolean exposesItems() {
        return this == INTERFACE || this == VOID;
    }

    public String getRegistryName() {
        return "chest_wall_" + getName();
    }
}
