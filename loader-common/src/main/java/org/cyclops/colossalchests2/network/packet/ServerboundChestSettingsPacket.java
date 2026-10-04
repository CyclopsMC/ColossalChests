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
 * The search query and settings from the chest GUI.
 * @author rubensworks
 */
public class ServerboundChestSettingsPacket extends PacketBase<ServerboundChestSettingsPacket> {

    public static final Type<ServerboundChestSettingsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChestSettingsPacket> CODEC = getCodec(ServerboundChestSettingsPacket::new);

    private int containerId;
    private String query;
    private ChestSettings settings;

    public ServerboundChestSettingsPacket() {
        super(TYPE);
    }

    public ServerboundChestSettingsPacket(int containerId, String query, ChestSettings settings) {
        super(TYPE);
        this.containerId = containerId;
        this.query = query;
        this.settings = settings;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeUtf(query, ContainerChest.MAX_QUERY_LENGTH);
        ChestSettings.STREAM_CODEC.encode(buf, settings);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        query = buf.readUtf(ContainerChest.MAX_QUERY_LENGTH);
        settings = ChestSettings.STREAM_CODEC.decode(buf);
    }

    @Override
    public void actionClient(Level level, Player player) {
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId && menu.stillValid(player)) {
            menu.handleSettings(query, settings);
        }
    }
}
