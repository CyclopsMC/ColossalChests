package org.cyclops.colossalchests2.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * Dragging the cursor stack over chest slots in the GUI.
 * @author rubensworks
 */
public class ServerboundChestDragPacket extends PacketBase<ServerboundChestDragPacket> {

    public static final Type<ServerboundChestDragPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_drag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChestDragPacket> CODEC = getCodec(ServerboundChestDragPacket::new);

    private int containerId;
    private int[] slots;
    private boolean oneEach;

    public ServerboundChestDragPacket() {
        super(TYPE);
    }

    public ServerboundChestDragPacket(int containerId, int[] slots, boolean oneEach) {
        super(TYPE);
        this.containerId = containerId;
        this.slots = slots;
        this.oneEach = oneEach;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarIntArray(slots);
        buf.writeBoolean(oneEach);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        slots = buf.readVarIntArray(ContainerChest.MAX_DRAG_SLOTS);
        oneEach = buf.readBoolean();
    }

    @Override
    public void actionClient(Level level, Player player) {
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId && menu.stillValid(player)) {
            menu.handleChestDrag(slots, oneEach);
        }
    }
}
