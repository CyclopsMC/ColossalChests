package org.cyclops.colossalchests.network.packet;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

import java.util.ArrayList;

/**
 * Packet for window clicks to the server as an alternative to
 * {@link CClickWindowPacket}.
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
	private String mode;

    public ClickWindowPacketOverride() {

    }

    public ClickWindowPacketOverride(int windowId, int slotId, int usedButton, ClickType mode, ItemStack clickedItem, short actionNumber) {
        this.windowId = windowId;
		this.slotId = slotId;
		this.usedButton = usedButton;
		this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
		this.actionNumber = actionNumber;
		this.mode = mode.name();
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {

    }

	// Adapted from ServerPlayNetHandler#processClickWindow
	@Override
	public void actionServer(World world, ServerPlayerEntity player) {
		player.resetLastActionTime();
        if (player.containerMenu.containerId == windowId && player.containerMenu.isSynched(player)) {
			if (player.isSpectator()) {
				ArrayList arraylist = Lists.newArrayList();

				for (int i = 0; i < player.containerMenu.slots.size(); ++i) {
					arraylist.add((player.containerMenu.slots.get(i)).getItem());
				}

				((ContainerColossalChest) player.containerMenu).updateCraftingInventory(player, arraylist);
			} else {
				ItemStack itemstack = player.containerMenu.clicked(slotId, usedButton, ClickType.valueOf(mode), player);

				if (ItemStack.matches(clickedItem, itemstack)) {
					player.connection.send(new SConfirmTransactionPacket(windowId, actionNumber, true));
					player.ignoreSlotUpdateHack = true;
					player.containerMenu.broadcastChanges();
					player.broadcastCarriedItem();
					player.ignoreSlotUpdateHack = false;
				} else {
					Int2ShortMap pendingTransactions = player.connection.expectedAcks;
					pendingTransactions.put(player.containerMenu.containerId, actionNumber);
					player.connection.send(new SConfirmTransactionPacket(windowId, actionNumber, false));
					player.containerMenu.setSynched(player, false);
					NonNullList<ItemStack> nonnulllist1 = NonNullList.create();

					for (int j = 0; j < player.containerMenu.slots.size(); ++j) {
						nonnulllist1.add(player.containerMenu.slots.get(j).getItem());
					}

					((ContainerColossalChest) player.containerMenu).updateCraftingInventory(player, nonnulllist1);
				}
			}
		}
	}
	
}