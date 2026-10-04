package org.cyclops.colossalchests2.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.inventory.ChestClickAction;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * A click on a chest slot in the GUI.
 * @author rubensworks
 */
public class ServerboundChestClickPacket extends PacketBase<ServerboundChestClickPacket> {

    public static final Type<ServerboundChestClickPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_click"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChestClickPacket> CODEC = getCodec(ServerboundChestClickPacket::new);

    private int containerId;
    private int slot;
    private ChestClickAction action;

    public ServerboundChestClickPacket() {
        super(TYPE);
    }

    public ServerboundChestClickPacket(int containerId, int slot, ChestClickAction action) {
        super(TYPE);
        this.containerId = containerId;
        this.slot = slot;
        this.action = action;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slot);
        buf.writeEnum(action);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        slot = buf.readVarInt();
        action = buf.readEnum(ChestClickAction.class);
    }

    @Override
    public void actionClient(Level level, Player player) {
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId && menu.stillValid(player)) {
            menu.handleChestClick(player, slot, action);
        }
    }
}
