package org.cyclops.colossalchests.network.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

/**
 * Packet for sending fragmented window items as an alternative to
 * {@link ClientboundContainerSetContentPacket}.
 * @author rubensworks
 *
 */
public class ClientboundContainerSetContentPacketWindow extends PacketCodec {

	@CodecField
	private int windowId;
	@CodecField
	private int stateId;
	@CodecField
	private CompoundTag itemStacks;

    public ClientboundContainerSetContentPacketWindow() {

    }

    public ClientboundContainerSetContentPacketWindow(int windowId, int stateId, CompoundTag itemStacks) {
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
			putStacksInSlotsWithOffset(player.inventoryMenu);
		} else if (windowId == player.containerMenu.containerId) {
			putStacksInSlotsWithOffset(player.containerMenu);
		}
	}

	protected void putStacksInSlotsWithOffset(AbstractContainerMenu container) {
		ListTag list = itemStacks.getList("stacks", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag tag = list.getCompound(i);
			int slot = tag.getInt("slot");
			ItemStack stack = ItemStack.of(tag.getCompound("stack"));
			container.setItem(slot, this.stateId, stack);
		}
	}

	@Override
	public void actionServer(Level world, ServerPlayer player) {

	}

}
