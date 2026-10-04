package org.cyclops.colossalchests2.proxy;

import org.cyclops.colossalchests2.ColossalChests;
import org.cyclops.colossalchests2.network.packet.ClientboundChestSlotsPacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestFormPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestSettingsPacket;
import org.cyclops.cyclopscore.init.ModBase;
import org.cyclops.cyclopscore.network.PacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return ColossalChests._instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        super.registerPacketHandlers(packetHandler);
        packetHandler.register(ClientboundChestSlotsPacket.class, ClientboundChestSlotsPacket.TYPE, ClientboundChestSlotsPacket.CODEC);
        packetHandler.register(ClientboundChestStatePacket.class, ClientboundChestStatePacket.TYPE, ClientboundChestStatePacket.CODEC);
        packetHandler.register(ServerboundChestClickPacket.class, ServerboundChestClickPacket.TYPE, ServerboundChestClickPacket.CODEC);
        packetHandler.register(ServerboundChestFormPacket.class, ServerboundChestFormPacket.TYPE, ServerboundChestFormPacket.CODEC);
        packetHandler.register(ServerboundChestDragPacket.class, ServerboundChestDragPacket.TYPE, ServerboundChestDragPacket.CODEC);
        packetHandler.register(ServerboundChestSettingsPacket.class, ServerboundChestSettingsPacket.TYPE, ServerboundChestSettingsPacket.CODEC);
    }
}
