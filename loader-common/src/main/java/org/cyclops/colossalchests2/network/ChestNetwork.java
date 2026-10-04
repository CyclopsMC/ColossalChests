package org.cyclops.colossalchests2.network;

import net.minecraft.server.level.ServerPlayer;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.cyclopscore.network.PacketBase;

import java.util.function.BiPredicate;

/**
 * Sends this mod's packets to players.
 * @author rubensworks
 */
public final class ChestNetwork {

    /**
     * If a player's connection accepts a packet. NeoForge only accepts packets on channels the client negotiated,
     * which connections without a real client, such as fake players, never do.
     */
    public static BiPredicate<ServerPlayer, PacketBase<?>> canReceive = (player, packet) -> true;

    private ChestNetwork() {
    }

    public static void sendToPlayer(PacketBase<?> packet, ServerPlayer player) {
        if (canReceive.test(player, packet)) {
            ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToPlayer(packet, player);
        }
    }

}
