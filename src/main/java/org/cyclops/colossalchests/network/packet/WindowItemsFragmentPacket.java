package org.cyclops.colossalchests.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

/**
 * Packet for sending fragmented window items as an alternative to
 * {@link SWindowItemsPacket}.
 * @author rubensworks
 *
 */
public class WindowItemsFragmentPacket extends PacketCodec {

	@CodecField
	private int windowId;
	@CodecField
	private CompoundNBT itemStacks;

    public WindowItemsFragmentPacket() {

    }

    public WindowItemsFragmentPacket(int windowId, CompoundNBT itemStacks) {
		this.windowId = windowId;
		this.itemStacks = itemStacks;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {
		// Modified code from NetHandlerPlayClient#handleWindowItems
		if (windowId == 0) {
			putStacksInSlotsWithOffset(player.container);
		} else if (windowId == player.openContainer.windowId) {
			putStacksInSlotsWithOffset(player.openContainer);
		}
	}

	protected void putStacksInSlotsWithOffset(Container container) {
		ListNBT list = itemStacks.getList("stacks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT tag = list.getCompound(i);
			int slot = tag.getInt("slot");
			ItemStack stack = ItemStack.read(tag.getCompound("stack"));
			container.putStackInSlot(slot, stack);
		}
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {

	}
	
}