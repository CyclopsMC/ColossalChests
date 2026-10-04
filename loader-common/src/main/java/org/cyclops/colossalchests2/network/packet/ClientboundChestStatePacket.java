package org.cyclops.colossalchests2.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * The chest state a viewer needs besides slots: depth, settings, and the slots to show in display order.
 * @author rubensworks
 */
public class ClientboundChestStatePacket extends PacketBase<ClientboundChestStatePacket> {

    public static final Type<ClientboundChestStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChestStatePacket> CODEC = getCodec(ClientboundChestStatePacket::new);

    private int containerId;
    private long depth;
    private ChestSettings settings;
    private int[] view;

    public ClientboundChestStatePacket() {
        super(TYPE);
    }

    public ClientboundChestStatePacket(int containerId, long depth, ChestSettings settings, int[] view) {
        super(TYPE);
        this.containerId = containerId;
        this.depth = depth;
        this.settings = settings;
        this.view = view;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarLong(depth);
        ChestSettings.STREAM_CODEC.encode(buf, settings);
        buf.writeVarIntArray(view);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        depth = buf.readVarLong();
        settings = ChestSettings.STREAM_CODEC.decode(buf);
        view = buf.readVarIntArray();
    }

    @Override
    public void actionClient(Level level, Player player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId) {
            menu.applyState(depth, settings, view);
        }
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
    }
}
