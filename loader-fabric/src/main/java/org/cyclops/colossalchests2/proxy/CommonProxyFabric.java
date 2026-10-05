package org.cyclops.colossalchests2.proxy;

import org.cyclops.colossalchests2.ColossalChestsFabric;
import org.cyclops.colossalchests2.network.packet.ClientboundChestSlotsPacket;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestClickPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestDragPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundChestFormPacket;
import org.cyclops.colossalchests2.network.packet.ServerboundDisplayTakePacket;
import org.cyclops.cyclopscore.init.ModBaseFabric;
import org.cyclops.cyclopscore.network.IPacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponentFabric;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxyFabric extends CommonProxyComponentFabric {

    @Override
    public ModBaseFabric<?> getMod() {
        return ColossalChestsFabric._instance;
    }

    @Override
    public void registerPackets(IPacketHandler packetHandler) {
        super.registerPackets(packetHandler);
        packetHandler.register(ClientboundChestSlotsPacket.class, ClientboundChestSlotsPacket.TYPE, ClientboundChestSlotsPacket.CODEC);
        packetHandler.register(ClientboundChestStatePacket.class, ClientboundChestStatePacket.TYPE, ClientboundChestStatePacket.CODEC);
        packetHandler.register(ServerboundChestClickPacket.class, ServerboundChestClickPacket.TYPE, ServerboundChestClickPacket.CODEC);
        packetHandler.register(ServerboundChestFormPacket.class, ServerboundChestFormPacket.TYPE, ServerboundChestFormPacket.CODEC);
        packetHandler.register(ServerboundDisplayTakePacket.class, ServerboundDisplayTakePacket.TYPE, ServerboundDisplayTakePacket.CODEC);
        packetHandler.register(ServerboundChestDragPacket.class, ServerboundChestDragPacket.TYPE, ServerboundChestDragPacket.CODEC);
    }
}
