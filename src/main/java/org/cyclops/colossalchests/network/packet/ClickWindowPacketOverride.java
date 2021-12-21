package org.cyclops.colossalchests.network.packet;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
public class ClickWindowPacketOverride extends PacketCodec {

	@CodecField
	private int windowId;
	@CodecField
	private int slotId;
	@CodecField
	private int usedButton;
	@CodecField
	private ItemStack clickedItem;
	@CodecField
	private String mode;

    public ClickWindowPacketOverride() {

    }

    public ClickWindowPacketOverride(int windowId, int slotId, int usedButton, ClickType mode, ItemStack clickedItem) {
        this.windowId = windowId;
		this.slotId = slotId;
		this.usedButton = usedButton;
		this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
		this.mode = mode.name();
    }

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
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
				boolean flag = ItemStack.matches(clickedItem, player.containerMenu.getCarried());
				player.containerMenu.suppressRemoteUpdates();
				player.containerMenu.clicked(slotId, usedButton, ClickType.valueOf(mode), player);

				/*for(Int2ObjectMap.Entry<ItemStack> entry : Int2ObjectMaps.fastIterable(p_9856_.getChangedSlots())) {
					player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), entry.getValue());
				}*/

				player.containerMenu.setRemoteCarried(clickedItem);
				player.containerMenu.resumeRemoteUpdates();
				if (!flag) {
					//player.containerMenu.broadcastFullState();

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

		// TODO: rm
        /*if (player.containerMenu.containerId == windowId && player.containerMenu.stillValid(player)) {
			if (player.isSpectator()) {
				ArrayList arraylist = Lists.newArrayList();

				for (int i = 0; i < player.containerMenu.slots.size(); ++i) {
					arraylist.add((player.containerMenu.slots.get(i)).getItem());
				}

				((ContainerColossalChest) player.containerMenu).updateCraftingInventory(player, arraylist);
			} else {
				player.containerMenu.clicked(slotId, usedButton, ClickType.valueOf(mode), player);
				ItemStack itemstack = player.containerMenu.getCarried();

				if (ItemStack.matches(clickedItem, itemstack)) {
					player.connection.send(new ClientboundContainerAckPacket(windowId, actionNumber, true));
					player.ignoreSlotUpdateHack = true;
					player.containerMenu.broadcastChanges();
					player.broadcastCarriedItem();
					player.ignoreSlotUpdateHack = false;
				} else {
					Int2ShortMap pendingTransactions = player.connection.expectedAcks;
					pendingTransactions.put(player.containerMenu.containerId, actionNumber);
					player.connection.send(new ClientboundContainerAckPacket(windowId, actionNumber, false));
					player.containerMenu.setSynched(player, false);
					NonNullList<ItemStack> nonnulllist1 = NonNullList.create();

					for (int j = 0; j < player.containerMenu.slots.size(); ++j) {
						nonnulllist1.add(player.containerMenu.slots.get(j).getItem());
					}

					((ContainerColossalChest) player.containerMenu).updateCraftingInventory(player, nonnulllist1);
				}
			}
		}*/
	}

}
