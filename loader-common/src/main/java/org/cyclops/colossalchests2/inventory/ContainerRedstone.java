package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.jetbrains.annotations.Nullable;

/**
 * The settings of a Redstone wall: the item type it signals for, empty for the whole chest, and its current signal.
 * @author rubensworks
 */
public class ContainerRedstone extends ContainerGhostSettings {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 136;
    public static final int TARGET_X = 8;
    public static final int TARGET_Y = 22;
    public static final int INVENTORY_Y = 54;

    private final DataSlot signal;

    /**
     * Client-side constructor.
     */
    public ContainerRedstone(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos(), new SimpleContainer(1), null);
    }

    /**
     * Server-side constructor.
     */
    public ContainerRedstone(int id, Inventory inventory, BlockEntityChestWall wall) {
        this(id, inventory, wall.getBlockPos(), wall.getRedstoneTarget(), wall);
    }

    private ContainerRedstone(int id, Inventory inventory, BlockPos pos, Container target, @Nullable BlockEntityChestWall wall) {
        super(RegistryEntries.MENU_REDSTONE.value(), id, wall);
        addGhostSlot(target, 0, TARGET_X, TARGET_Y);
        addPlayerInventory(inventory, INVENTORY_Y);
        this.signal = addDataSlot(wall == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return wall.getComparatorSignal();
            }

            @Override
            public void set(int value) {
            }
        });
    }

    /**
     * @return The wall's comparator signal, from 0 to 15.
     */
    public int getSignal() {
        return signal.get();
    }
}
