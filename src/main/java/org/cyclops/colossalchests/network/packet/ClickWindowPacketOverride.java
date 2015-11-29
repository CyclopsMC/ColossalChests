package org.cyclops.colossalchests.network.packet;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

import java.util.ArrayList;

/**
 * Packet for window clicks to the server as an alternative to
 * {@link net.minecraft.network.play.client.C0EPacketClickWindow}.
 * @author rubensworks
 *
 */
public class ClickWindowPacketOverride extends PacketCodec {

	@CodecField
	private int windowId;
	@CodecField
	private int slotId;
	@CodecField
	private int usedButton;
	@CodecField
	private short actionNumber;
	@CodecField
	private ItemStack clickedItem;
	@CodecField
	private int mode;

    public ClickWindowPacketOverride() {

    }

    public ClickWindowPacketOverride(int windowId, int slotId, int usedButton, int mode, ItemStack clickedItem, short actionNumber) {
        this.windowId = windowId;
		this.slotId = slotId;
		this.usedButton = usedButton;
		this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
		this.actionNumber = actionNumber;
		this.mode = mode;
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void actionClient(World world, EntityPlayer player) {

    }

	// Adapted from NetHandlerPlayServer#processClickWindow
	@Override
	public void actionServer(World world, EntityPlayerMP player) {
		player.markPlayerActive();
        if (player.openContainer.windowId == windowId && player.openContainer.getCanCraft(player)) {
			if (player.isSpectator()) {
				ArrayList arraylist = Lists.newArrayList();

				for (int i = 0; i < player.openContainer.inventorySlots.size(); ++i) {
					arraylist.add(((Slot)player.openContainer.inventorySlots.get(i)).getStack());
				}

				((ContainerColossalChest) player.openContainer).updateCraftingInventory(player, arraylist);
			} else {
				ItemStack itemstack = player.openContainer.slotClick(slotId, usedButton, mode, player);

				if (ItemStack.areItemStacksEqual(clickedItem, itemstack)) {
					player.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(windowId, actionNumber, true));
					player.isChangingQuantityOnly = true;
					player.openContainer.detectAndSendChanges();
					player.updateHeldItem();
					player.isChangingQuantityOnly = false;
				} else {
					IntHashMap field_147372_n = ReflectionHelper.getPrivateValue(NetHandlerPlayServer.class, player.playerNetServerHandler, "field_147372_n");
					field_147372_n.addKey(player.openContainer.windowId, Short.valueOf(actionNumber));
					player.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(windowId, actionNumber, false));
					player.openContainer.setCanCraft(player, false);
					ArrayList arraylist1 = Lists.newArrayList();

					for (int j = 0; j < player.openContainer.inventorySlots.size(); ++j) {
						arraylist1.add(((Slot)player.openContainer.inventorySlots.get(j)).getStack());
					}

					((ContainerColossalChest) player.openContainer).updateCraftingInventory(player, arraylist1);
				}
			}
		}
	}
	
}