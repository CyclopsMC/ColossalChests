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
 * Packet for setting slots with id's larger than max short size (65535).
 * {@link net.minecraft.network.play.server.SPacketSetSlot}.
 * @author rubensworks
 *
 */
public class SetSlotLarge extends PacketCodec {

	@CodecField
	private int windowId;
	@CodecField
	private int slot;
	@CodecField
	private ItemStack itemStack;

    public SetSlotLarge() {

    }

    public SetSlotLarge(int windowId, int slot, ItemStack itemStack) {
		this.windowId = windowId;
		this.slot = slot;
		this.itemStack = itemStack;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {
		// Modified code from NetHandlerPlayClient#handleSetSlot
		if (windowId == player.openContainer.windowId) {
			player.openContainer.putStackInSlot(slot, itemStack);
		}
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}