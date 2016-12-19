package org.cyclops.colossalchests.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

/**
 * Packet for sending fragmented window items as an alternative to
 * {@link net.minecraft.network.play.server.SPacketWindowItems}.
 * @author rubensworks
 *
 */
public class WindowItemsFragmentPacket extends PacketCodec {

	@CodecField
	private int windowId;
	@CodecField
	private NBTTagCompound itemStacks;

    public WindowItemsFragmentPacket() {

    }

    public WindowItemsFragmentPacket(int windowId, NBTTagCompound itemStacks) {
		this.windowId = windowId;
		this.itemStacks = itemStacks;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		// Modified code from NetHandlerPlayClient#handleWindowItems
		if (windowId == 0) {
			putStacksInSlotsWithOffset(player.inventoryContainer);
		} else if (windowId == player.openContainer.windowId) {
			putStacksInSlotsWithOffset(player.openContainer);
		}
	}

	protected void putStacksInSlotsWithOffset(Container container) {
		NBTTagList list = itemStacks.getTagList("stacks", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal());
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int slot = tag.getInteger("slot");
			ItemStack stack = new ItemStack(tag.getCompoundTag("stack"));
			container.putStackInSlot(slot, stack);
		}
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}