package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetContentPacketWindow;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetSlotPacketLarge;
import org.cyclops.colossalchests.network.packet.ServerboundContainerClickPacketOverride;
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

        // Register packets.
        packetHandler.register(ClientboundContainerSetContentPacketWindow.TYPE, ClientboundContainerSetContentPacketWindow.CODEC);
        packetHandler.register(ServerboundContainerClickPacketOverride.TYPE, ServerboundContainerClickPacketOverride.CODEC);
        packetHandler.register(ClientboundContainerSetSlotPacketLarge.TYPE, ClientboundContainerSetSlotPacketLarge.CODEC);
    }

}
