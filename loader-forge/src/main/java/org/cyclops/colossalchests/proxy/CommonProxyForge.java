package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChestsForge;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetContentPacketWindow;
import org.cyclops.colossalchests.network.packet.ClientboundContainerSetSlotPacketLarge;
import org.cyclops.colossalchests.network.packet.ServerboundContainerClickPacketOverride;
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

        // Register packets.
        packetHandler.register(ClientboundContainerSetContentPacketWindow.class, ClientboundContainerSetContentPacketWindow.TYPE, ClientboundContainerSetContentPacketWindow.CODEC);
        packetHandler.register(ServerboundContainerClickPacketOverride.class, ServerboundContainerClickPacketOverride.TYPE, ServerboundContainerClickPacketOverride.CODEC);
        packetHandler.register(ClientboundContainerSetSlotPacketLarge.class, ClientboundContainerSetSlotPacketLarge.TYPE, ClientboundContainerSetSlotPacketLarge.CODEC);
    }
}
