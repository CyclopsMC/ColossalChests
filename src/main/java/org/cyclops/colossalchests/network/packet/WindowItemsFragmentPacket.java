package org.cyclops.colossalchests.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

import java.util.List;

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
	private int offset;
	@CodecField
	private List<ItemStack> itemStacks;

    public WindowItemsFragmentPacket() {

    }

    public WindowItemsFragmentPacket(int windowId, int offset, List<ItemStack> itemStacks) {
		this.windowId = windowId;
        this.offset = offset;
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
		for(int i = offset; i < offset + itemStacks.size(); i++) {
			container.putStackInSlot(i, itemStacks.get(i - offset));
		}
	}

	@Override
	public void actionServer(World world, EntityPlayerMP player) {

	}
	
}