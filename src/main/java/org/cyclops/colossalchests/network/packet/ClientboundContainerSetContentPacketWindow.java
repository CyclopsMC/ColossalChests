package org.cyclops.colossalchests.network.packet;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.Reference;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

/**
 * Packet for sending fragmented window items as an alternative to
 * {@link ClientboundContainerSetContentPacket}.
 * @author rubensworks
 *
 */
public class ClientboundContainerSetContentPacketWindow extends PacketCodec<ClientboundContainerSetContentPacketWindow> {

	public static final Type<ClientboundContainerSetContentPacketWindow> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "clientbound_container_set_content_packet_window"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetContentPacketWindow> CODEC = getCodec(ClientboundContainerSetContentPacketWindow::new);

	@CodecField
	private int windowId;
	@CodecField
	private int stateId;
	@CodecField
	private CompoundTag itemStacks;

    public ClientboundContainerSetContentPacketWindow() {
		super(TYPE);
    }

    public ClientboundContainerSetContentPacketWindow(int windowId, int stateId, CompoundTag itemStacks) {
		super(TYPE);
		this.windowId = windowId;
		this.stateId = stateId;
		this.itemStacks = itemStacks;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(Level world, Player player) {
		// Modified code from NetHandlerPlayClient#handleWindowItems
		if (windowId == 0) {
			putStacksInSlotsWithOffset(world.registryAccess(), player.inventoryMenu);
		} else if (windowId == player.containerMenu.containerId) {
			putStacksInSlotsWithOffset(world.registryAccess(), player.containerMenu);
		}
	}

	protected void putStacksInSlotsWithOffset(HolderLookup.Provider provider, AbstractContainerMenu container) {
		ListTag list = itemStacks.getList("stacks", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			int slot = tag.getInt("slot");
			ItemStack stack = ItemStack.parseOptional(provider, tag.getCompound("stack"));
			container.setItem(slot, this.stateId, stack);
		}
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {

	}

}
