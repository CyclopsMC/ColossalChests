package org.cyclops.colossalchests2.inventory;

/**
 * What a click on a chest slot in the GUI does.
 * @author rubensworks
 */
public enum ChestClickAction {
    /**
     * Left click: take one max stack onto the cursor, or put the whole cursor in.
     */
    TAKE_STACK,
    /**
     * Right click: take half a max stack onto the cursor, or put one cursor item in.
     */
    TAKE_HALF,
    /**
     * Shift click: move one max stack into the player inventory.
     */
    MOVE_STACK,
    /**
     * Ctrl click: move as many items as fit into the player inventory.
     */
    MOVE_ALL
}
