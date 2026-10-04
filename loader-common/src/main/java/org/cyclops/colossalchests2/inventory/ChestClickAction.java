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
    MOVE_ALL,
    /**
     * Alt click: lock or unlock the slot. Needs the Lock upgrade.
     */
    TOGGLE_LOCK,
    /**
     * Right click on an empty slot with a cursor item: lock the slot to that item without inserting. Needs the Lock upgrade.
     */
    LOCK_TO_CURSOR,
    /**
     * Lock all filled slots, the slot index is ignored. Needs the Lock upgrade.
     */
    LOCK_ALL,
    /**
     * Unlock all slots, the slot index is ignored. Needs the Lock upgrade.
     */
    CLEAR_LOCKS,
    /**
     * Alt right click: mark or unmark the slot as voiding. Needs the Void upgrade.
     */
    TOGGLE_VOID,
    /**
     * Mark all filled slots as voiding, the slot index is ignored. Needs the Void upgrade.
     */
    VOID_ALL,
    /**
     * Unmark all voiding slots, the slot index is ignored. Needs the Void upgrade.
     */
    CLEAR_VOIDS;

    /**
     * @return If this action changes locks or void marks instead of moving items.
     */
    public boolean isMarkAction() {
        return ordinal() >= TOGGLE_LOCK.ordinal();
    }
}
