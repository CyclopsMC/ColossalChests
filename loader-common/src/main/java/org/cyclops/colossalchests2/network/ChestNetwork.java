package org.cyclops.colossalchests2.network;

import net.minecraft.server.level.ServerPlayer;
import org.cyclops.colossalchests2.ColossalChestsInstance;
import org.cyclops.colossalchests2.network.packet.ClientboundChestStatePacket;
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

    /**
     * @param player A player.
     * @return If the player has a real client with this mod, so menus can be opened for them.
     */
    public static boolean hasClient(ServerPlayer player) {
        return canReceive.test(player, new ClientboundChestStatePacket());
    }

    public static void sendToPlayer(PacketBase<?> packet, ServerPlayer player) {
        if (canReceive.test(player, packet)) {
            ColossalChestsInstance.MOD.getPacketHandlerCommon().sendToPlayer(packet, player);
        }
    }

}
