package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChestsNeoForge;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetContentPacketWindow;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetSlotPacketLarge;
import org.cyclops.colossalchests.network.packet.ServerboundContainerClickPacketOverride;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;
import org.cyclops.cyclopscore.network.IPacketHandler;
import org.cyclops.cyclopscore.proxy.CommonProxyComponent;

/**
 * Proxy for server and client side.
 * @author rubensworks
 *
 */
public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBaseNeoForge<ColossalChestsNeoForge> getMod() {
        return ColossalChestsNeoForge._instance;
    }

    @Override
    public void registerPackets(IPacketHandler packetHandler) {
        super.registerPackets(packetHandler);

        // Register packets.
        packetHandler.register(ClientboundContainerSetContentPacketWindow.class, ClientboundContainerSetContentPacketWindow.TYPE, ClientboundContainerSetContentPacketWindow.CODEC);
        packetHandler.register(ServerboundContainerClickPacketOverride.class, ServerboundContainerClickPacketOverride.TYPE, ServerboundContainerClickPacketOverride.CODEC);
        packetHandler.register(ClientboundContainerSetSlotPacketLarge.class, ClientboundContainerSetSlotPacketLarge.TYPE, ClientboundContainerSetSlotPacketLarge.CODEC);
    }

}
