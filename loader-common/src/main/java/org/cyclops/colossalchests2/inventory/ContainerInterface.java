package org.cyclops.colossalchests2.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.capability.WallAccess;
import org.jetbrains.annotations.Nullable;

/**
 * The settings of an Interface: filter slots and direction.
 * @author rubensworks
 */
public class ContainerInterface extends ContainerGhostSettings {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 154;
    public static final int FILTER_Y = 18;
    public static final int MODE_Y = 38;
    public static final int INVENTORY_Y = 72;
    public static final int BUTTON_MODE = 0;

    private final BlockPos pos;
    private final DataSlot mode;

    /**
     * Client-side constructor.
     */
    public ContainerInterface(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, data.readBlockPos(), new SimpleContainer(BlockEntityChestWall.FILTER_SLOTS), null);
    }

    /**
     * Server-side constructor.
     */
    public ContainerInterface(int id, Inventory inventory, BlockEntityChestWall wall) {
        this(id, inventory, wall.getBlockPos(), wall.getSettings(), wall);
    }

    private ContainerInterface(int id, Inventory inventory, BlockPos pos, Container settings, @Nullable BlockEntityChestWall wall) {
        super(RegistryEntries.MENU_INTERFACE.value(), id, wall);
        this.pos = pos;
        for (int i = 0; i < BlockEntityChestWall.FILTER_SLOTS; i++) {
            addGhostSlot(settings, i, 8 + i * 18, FILTER_Y);
        }
        addPlayerInventory(inventory, INVENTORY_Y);
        this.mode = wall == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return wall.getMode().ordinal();
            }

            @Override
            public void set(int value) {
                wall.setMode(WallAccess.Mode.values()[value]);
            }
        };
        addDataSlot(this.mode);
    }

    public BlockPos getPos() {
        return pos;
    }

    public WallAccess.Mode getMode() {
        WallAccess.Mode[] modes = WallAccess.Mode.values();
        return modes[Math.floorMod(mode.get(), modes.length)];
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_MODE && wall != null) {
            wall.setMode(wall.getMode().next());
            return true;
        }
        return false;
    }
}
