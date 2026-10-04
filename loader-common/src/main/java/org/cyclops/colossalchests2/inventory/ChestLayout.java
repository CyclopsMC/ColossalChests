package org.cyclops.colossalchests2.inventory;

/**
 * Positions in the chest GUI, shared by the menu and the screen.
 * The grid has 9 columns up to 54 slots and 18 above that, so 81 slots still fit a 240 pixel high screen.
 * @author rubensworks
 */
public record ChestLayout(int slotCount, int columns, int rows) {

    public static final int SLOT_SIZE = 18;
    public static final int GRID_Y = 34;
    private static final int BORDER = 7;
    private static final int PLAYER_INVENTORY_WIDTH = 9 * SLOT_SIZE;
    private static final int PLAYER_INVENTORY_HEIGHT = 83;

    public static ChestLayout of(int slotCount) {
        int columns = slotCount <= 54 ? 9 : 18;
        int rows = Math.max(1, (slotCount + columns - 1) / columns);
        return new ChestLayout(slotCount, columns, rows);
    }

    public int getWidth() {
        return columns * SLOT_SIZE + 2 * BORDER;
    }

    public int getHeight() {
        return getPlayerInventoryY() + PLAYER_INVENTORY_HEIGHT;
    }

    public int getGridX() {
        return BORDER + 1;
    }

    /**
     * @param position A position in the grid.
     * @return The x of the slot's item, relative to the screen.
     */
    public int getSlotX(int position) {
        return getGridX() + (position % columns) * SLOT_SIZE;
    }

    public int getSlotY(int position) {
        return GRID_Y + (position / columns) * SLOT_SIZE;
    }

    public int getPlayerInventoryX() {
        return (getWidth() - PLAYER_INVENTORY_WIDTH) / 2 + 1;
    }

    public int getPlayerInventoryY() {
        return GRID_Y + rows * SLOT_SIZE + 14;
    }

    /**
     * @param x A x relative to the screen.
     * @param y A y relative to the screen.
     * @return The grid position at it, or -1.
     */
    public int getPositionAt(double x, double y) {
        int column = (int) Math.floor((x - getGridX() + 1) / SLOT_SIZE);
        int row = (int) Math.floor((y - GRID_Y + 1) / SLOT_SIZE);
        if (column < 0 || column >= columns || row < 0 || row >= rows) {
            return -1;
        }
        int position = row * columns + column;
        return position < slotCount ? position : -1;
    }

}
