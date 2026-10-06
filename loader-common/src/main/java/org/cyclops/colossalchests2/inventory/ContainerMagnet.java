package org.cyclops.colossalchests2.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import org.cyclops.colossalchests2.GeneralConfig;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.jetbrains.annotations.Nullable;

/**
 * The settings of a Magnet wall: its radius, changed by buttons.
 * @author rubensworks
 */
public class ContainerMagnet extends AbstractContainerMenu {

    public static final int BUTTON_DECREASE = 0;
    public static final int BUTTON_INCREASE = 1;
    public static final int BUTTON_DECREASE_MORE = 2;
    public static final int BUTTON_INCREASE_MORE = 3;
    public static final int STEP_MORE = 8;

    @Nullable
    private final BlockEntityChestWall wall;
    private final DataSlot radius;
    private final DataSlot maxRadius;

    /**
     * Client-side constructor.
     */
    public ContainerMagnet(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, null);
        data.readBlockPos();
    }

    /**
     * Server-side constructor.
     */
    public ContainerMagnet(int id, Inventory inventory, BlockEntityChestWall wall) {
        this(id, wall);
    }

    private ContainerMagnet(int id, @Nullable BlockEntityChestWall wall) {
        super(RegistryEntries.MENU_MAGNET.value(), id);
        this.wall = wall;
        this.radius = addDataSlot(wall == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return wall.getMagnetRadius();
            }

            @Override
            public void set(int value) {
            }
        });
        this.maxRadius = addDataSlot(wall == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return GeneralConfig.getMagnetMaxRadius();
            }

            @Override
            public void set(int value) {
            }
        });
    }

    public int getRadius() {
        return radius.get();
    }

    public int getMaxRadius() {
        return maxRadius.get();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (wall == null) {
            return false;
        }
        int change = switch (id) {
            case BUTTON_DECREASE -> -1;
            case BUTTON_INCREASE -> 1;
            case BUTTON_DECREASE_MORE -> -STEP_MORE;
            case BUTTON_INCREASE_MORE -> STEP_MORE;
            default -> 0;
        };
        if (change == 0) {
            return false;
        }
        wall.setMagnetRadius(wall.getMagnetRadius() + change);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return wall == null || (!wall.isRemoved() && Container.stillValidBlockEntity(wall, player));
    }
}
