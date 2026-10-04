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
import org.cyclops.cyclopscore.network.PacketBase;

/**
 * The form a player picked to take out of a compressed chest slot.
 * @author rubensworks
 */
public class ServerboundChestFormPacket extends PacketBase<ServerboundChestFormPacket> {

    public static final Type<ServerboundChestFormPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "chest_form"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundChestFormPacket> CODEC = getCodec(ServerboundChestFormPacket::new);

    private int containerId;
    private int slot;
    private ItemStack form;

    public ServerboundChestFormPacket() {
        super(TYPE);
    }

    public ServerboundChestFormPacket(int containerId, int slot, ItemStack form) {
        super(TYPE);
        this.containerId = containerId;
        this.slot = slot;
        this.form = form;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, form);
    }

    @Override
    public void decode(RegistryFriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        slot = buf.readVarInt();
        form = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
    }

    @Override
    public void actionClient(Level level, Player player) {
    }

    @Override
    public void actionServer(Level level, ServerPlayer player) {
        if (player.containerMenu instanceof ContainerChest menu && menu.containerId == containerId && menu.stillValid(player)) {
            menu.handleForm(slot, form);
        }
    }
}
