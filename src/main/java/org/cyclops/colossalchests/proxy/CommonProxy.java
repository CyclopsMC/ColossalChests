package org.cyclops.colossalchests.proxy;

import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.network.packet.ClickWindowPacketOverride;
import org.cyclops.colossalchests.network.packet.SetSlotLarge;
import org.cyclops.colossalchests.network.packet.WindowItemsFragmentPacket;
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
        packetHandler.register(WindowItemsFragmentPacket.class);
        packetHandler.register(ClickWindowPacketOverride.class);
        packetHandler.register(SetSlotLarge.class);
    }

}
