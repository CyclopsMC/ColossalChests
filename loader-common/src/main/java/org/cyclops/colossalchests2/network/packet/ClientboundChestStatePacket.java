package org.cyclops.colossalchests2.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.storage.CapacityProfile;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * The chest state a viewer needs besides slots: capacity rules and why upgrades can't be removed.
 * @author rubensworks
 */
public class ClientboundChestStatePacket extends PacketBase<ClientboundChestStatePacket> {

    public static final Type<ClientboundChestStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChestStatePacket> CODEC = getCodec(ClientboundChestStatePacket::new);

    private int containerId;
    private CapacityProfile profile;
    private int[] upgradeRemovalProblems;

    public ClientboundChestStatePacket() {
        super(TYPE);
    }

    public ClientboundChestStatePacket(int containerId, CapacityProfile profile, int[] upgradeRemovalProblems) {
        super(TYPE);
        this.containerId = containerId;
        this.profile = profile;
        this.upgradeRemovalProblems = upgradeRemovalProblems;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarLong(profile.depth());
        buf.writeVarLong(profile.maxItemsPerSlot());
        buf.writeBoolean(profile.acceptNonStackables());
        buf.writeVarLong(profile.nonStackableCapacity());
        buf.writeVarIntArray(upgradeRemovalProblems);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        profile = new CapacityProfile(buf.readVarLong(), buf.readVarLong(), buf.readBoolean(), buf.readVarLong());
        upgradeRemovalProblems = buf.readVarIntArray();
    }

    @Override
    public void actionClient(Level level, Player player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId) {
            menu.applyState(profile, upgradeRemovalProblems);
        }
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
    }
}
