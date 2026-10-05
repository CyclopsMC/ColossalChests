package org.cyclops.colossalchests2.inventory;

import com.google.common.collect.Lists;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestCore;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * The settings of a Display wall: for each face on the outside of the chest, the item it shows and if it is shown.
 * Buttons are numbered by face ordinal.
 * @author rubensworks
 */
public class ContainerDisplay extends ContainerGhostSettings {

    public static final int WIDTH = 176;
    public static final int HEIGHT = 168;
    public static final int COLUMN_WIDTH = 58;
    public static final int LABEL_Y = 18;
    public static final int DISPLAYED_Y = 30;
    public static final int BUTTON_Y = 50;
    public static final int BUTTON_WIDTH = 52;
    public static final int INVENTORY_Y = 86;

    private final Direction front;
    private final List<Direction> faces;
    private final DataSlot hiddenFaces;

    /**
     * Client-side constructor.
     */
    public ContainerDisplay(int id, Inventory inventory, FriendlyByteBuf data) {
        this(id, inventory, new SimpleContainer(BlockEntityChestWall.FACES), null, readFront(data), readFaces(data));
    }

    /**
     * Server-side constructor.
     */
    public ContainerDisplay(int id, Inventory inventory, BlockEntityChestWall wall) {
        this(id, inventory, wall.getDisplayedContainer(), wall, getFront(wall), wall.getDisplayFaces());
    }

    private ContainerDisplay(int id, Inventory inventory, Container displayed, @Nullable BlockEntityChestWall wall,
                             Direction front, List<Direction> faces) {
        super(RegistryEntries.MENU_DISPLAY.value(), id, wall);
        this.front = front;
        this.faces = faces;
        for (int i = 0; i < faces.size(); i++) {
            addGhostSlot(displayed, faces.get(i).ordinal(), getColumnX(i) - 8, DISPLAYED_Y);
        }
        addPlayerInventory(inventory, INVENTORY_Y);
        this.hiddenFaces = wall == null ? DataSlot.standalone() : new DataSlot() {
            @Override
            public int get() {
                return wall.getHiddenFaces();
            }

            @Override
            public void set(int value) {
            }
        };
        addDataSlot(this.hiddenFaces);
    }

    private static Direction getFront(BlockEntityChestWall wall) {
        return wall.getCore().map(BlockEntityChestCore::getFacing).orElse(Direction.NORTH);
    }

    /**
     * Write what the client constructor reads.
     */
    public static void writeOpenData(FriendlyByteBuf data, BlockEntityChestWall wall) {
        data.writeBlockPos(wall.getBlockPos());
        data.writeEnum(getFront(wall));
        List<Direction> faces = wall.getDisplayFaces();
        data.writeVarInt(faces.size());
        faces.forEach(data::writeEnum);
    }

    private static Direction readFront(FriendlyByteBuf data) {
        data.readBlockPos();
        return data.readEnum(Direction.class);
    }

    private static List<Direction> readFaces(FriendlyByteBuf data) {
        int count = data.readVarInt();
        List<Direction> faces = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; i++) {
            faces.add(data.readEnum(Direction.class));
        }
        return faces;
    }

    /**
     * @return The faces that can show an item, one column each.
     */
    public List<Direction> getFaces() {
        return faces;
    }

    /**
     * @param column A column index.
     * @return The horizontal center of the column.
     */
    public int getColumnX(int column) {
        return WIDTH / 2 - faces.size() * COLUMN_WIDTH / 2 + column * COLUMN_WIDTH + COLUMN_WIDTH / 2;
    }

    public boolean isFaceHidden(Direction face) {
        return (hiddenFaces.get() & (1 << face.ordinal())) != 0;
    }

    /**
     * @param face A face.
     * @return The translation key of the face's name, relative to the chest's front.
     */
    public String getFaceTranslationKey(Direction face) {
        String side;
        if (face.getAxis().isVertical()) {
            side = face == Direction.UP ? "top" : "bottom";
        } else if (face == front) {
            side = "front";
        } else if (face == front.getOpposite()) {
            side = "back";
        } else {
            // Seen while facing the front.
            side = face == front.getCounterClockWise() ? "right" : "left";
        }
        return "gui.colossalchests2.display.side." + side.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (wall != null && id >= 0 && id < BlockEntityChestWall.FACES) {
            Direction face = Direction.from3DDataValue(id);
            return faces.contains(face) && wall.setFaceHidden(face, !wall.isFaceHidden(face));
        }
        return false;
    }
}
