package org.cyclops.colossalchests.network.packet;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

import java.util.ArrayList;

/**
 * Packet for window clicks to the server as an alternative to
 * {@link ServerboundContainerClickPacket}.
 * @author rubensworks
 *
 */
public class ServerboundContainerClickPacketOverride extends PacketCodec<ServerboundContainerClickPacketOverride> {

    public static final Type<ServerboundContainerClickPacketOverride> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "serverbound_container_click_packet_override"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundContainerClickPacketOverride> CODEC = getCodec(ServerboundContainerClickPacketOverride::new);

    @CodecField
    private int windowId;
    @CodecField
    private int stateId;
    @CodecField
    private int slotId;
    @CodecField
    private int usedButton;
    @CodecField
    private HashedStack clickedItem;
    @CodecField
    private String mode;
    private Int2ObjectMap<HashedStack> changedSlots;

    public ServerboundContainerClickPacketOverride() {
        super(TYPE);
    }

    public ServerboundContainerClickPacketOverride(int windowId, int stateId, int slotId, int usedButton, ContainerInput mode, HashedStack clickedItem, Int2ObjectMap<HashedStack> changedSlots) {
        super(TYPE);
        this.windowId = windowId;
        this.stateId = stateId;
        this.slotId = slotId;
        this.usedButton = usedButton;
        this.clickedItem = clickedItem;
        this.mode = mode.name();
        this.changedSlots = changedSlots;
    }

    private static final int MAX_SLOT_COUNT = 128;

    // Keeps int keys rather than vanilla's shorts, since a colossal chest has far more than 32767 slots
    private static final StreamCodec<RegistryFriendlyByteBuf, Int2ObjectMap<HashedStack>> SLOTS_STREAM_CODEC =
            ByteBufCodecs.map(Int2ObjectOpenHashMap::new, ByteBufCodecs.VAR_INT, HashedStack.STREAM_CODEC, MAX_SLOT_COUNT);

    @Override
    public void encode(RegistryFriendlyByteBuf output) {
        super.encode(output);
        SLOTS_STREAM_CODEC.encode(output, this.changedSlots);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf input) {
        super.decode(input);
        this.changedSlots = Int2ObjectMaps.unmodifiable(SLOTS_STREAM_CODEC.decode(input));
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(Level world, Player player) {

    }

    // Adapted from ServerGamePacketListenerImpl#handleContainerClick
    @Override
    public void actionServer(Level world, ServerPlayer player) {
        player.resetLastActionTime();
        if (player.containerMenu.containerId == windowId) {
            if (player.isSpectator()) {
                ArrayList arraylist = Lists.newArrayList();

                for (int i = 0; i < player.containerMenu.slots.size(); ++i) {
                    arraylist.add((player.containerMenu.slots.get(i)).getItem());
                }

                ((ContainerColossalChest) player.containerMenu).updateCraftingInventory(player, arraylist);
            } else {
                boolean flag = this.stateId != player.containerMenu.getStateId();
                player.containerMenu.suppressRemoteUpdates();
                player.containerMenu.clicked(slotId, usedButton, ContainerInput.valueOf(mode), player);

                for(Int2ObjectMap.Entry<HashedStack> entry : Int2ObjectMaps.fastIterable(changedSlots)) {
                    player.containerMenu.setRemoteSlotUnsafe(entry.getIntKey(), entry.getValue());
                }

                player.containerMenu.setRemoteCarried(clickedItem);
                player.containerMenu.resumeRemoteUpdates();
                if (flag) {
                    // Original: player.containerMenu.broadcastFullState();

                    NonNullList<ItemStack> nonnulllist1 = NonNullList.create();

                    for (int j = 0; j < player.containerMenu.slots.size(); ++j) {
                        nonnulllist1.add(player.containerMenu.slots.get(j).getItem());
                    }

                    ((ContainerColossalChest) player.containerMenu).updateCraftingInventory(player, nonnulllist1);
                } else {
                    player.containerMenu.broadcastChanges();
                }
            }
        }
    }

}
