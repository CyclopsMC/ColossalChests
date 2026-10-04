package org.cyclops.colossalchests2.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.inventory.ContainerChest;
import org.cyclops.colossalchests2.storage.DeepSlot;
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * Changed chest slots, with long counts, for a player viewing the chest.
 * @author rubensworks
 */
public class ClientboundChestSlotsPacket extends PacketBase<ClientboundChestSlotsPacket> {

    public static final Type<ClientboundChestSlotsPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_slots"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundChestSlotsPacket> CODEC = getCodec(ClientboundChestSlotsPacket::new);

    private int containerId;
    private int[] slots;
    private DeepSlot[] contents;
    private long[] capacities;

    public ClientboundChestSlotsPacket() {
        super(TYPE);
    }

    public ClientboundChestSlotsPacket(int containerId, int[] slots, DeepSlot[] contents, long[] capacities) {
        super(TYPE);
        this.containerId = containerId;
        this.slots = slots;
        this.contents = contents;
        this.capacities = capacities;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slots.length);
        for (int i = 0; i < slots.length; i++) {
            buf.writeVarInt(slots[i]);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, contents[i].getPrototype());
            buf.writeVarLong(contents[i].getCount());
            buf.writeBoolean(contents[i].isLocked());
            buf.writeBoolean(contents[i].isVoiding());
            buf.writeVarLong(capacities[i]);
        }
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        int length = buf.readVarInt();
        slots = new int[length];
        contents = new DeepSlot[length];
        capacities = new long[length];
        for (int i = 0; i < length; i++) {
            slots[i] = buf.readVarInt();
            ItemStack prototype = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            long count = buf.readVarLong();
            boolean locked = buf.readBoolean();
            boolean voiding = buf.readBoolean();
            contents[i] = DeepSlot.of(prototype, count, locked, voiding, null);
            capacities[i] = buf.readVarLong();
        }
    }

    @Override
    public void actionClient(Level level, Player player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId) {
            menu.applySlots(slots, contents, capacities);
        }
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
    }
}
