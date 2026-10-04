package org.cyclops.colossalchests2.proxy;

import org.cyclops.colossalchests2.ColossalChestsForge;
import org.cyclops.colossalchests2.network.packet.ClientboundChestSlotsPacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestSettingsPacket;
import org.cyclops.cyclopscore.init.ModBaseForge;
import org.cyclops.cyclopscore.network.IPacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentForge;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyForge extends CommonProxyComponentForge {

    @Override
    public ModBaseForge<?> getMod() {
        return ColossalChestsForge._instance;
    }

    @Override
    public void registerPackets(IPacketHandler packetHandler) {
        super.registerPackets(packetHandler);
        packetHandler.register(ClientboundChestSlotsPacket.class, ClientboundChestSlotsPacket.TYPE, ClientboundChestSlotsPacket.CODEC);
        packetHandler.register(ClientboundChestStatePacket.class, ClientboundChestStatePacket.TYPE, ClientboundChestStatePacket.CODEC);
        packetHandler.register(ServerboundChestClickPacket.class, ServerboundChestClickPacket.TYPE, ServerboundChestClickPacket.CODEC);
        packetHandler.register(ServerboundChestDragPacket.class, ServerboundChestDragPacket.TYPE, ServerboundChestDragPacket.CODEC);
        packetHandler.register(ServerboundChestSettingsPacket.class, ServerboundChestSettingsPacket.TYPE, ServerboundChestSettingsPacket.CODEC);
    }
}
