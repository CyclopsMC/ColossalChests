package org.cyclops.colossalchests.network.packet;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.inventory.container.ContainerColossalChest;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

import java.util.ArrayList;
import java.util.function.IntFunction;

/**
 * Packet for window clicks to the server as an alternative to
 * {@link ServerboundContainerClickPacket}.
 * @author rubensworks
 *
 */
public class ServerboundContainerClickPacketOverride extends PacketCodec {

	public static final ResourceLocation ID = new ResourceLocation(Reference.MOD_ID, "serverbound_container_click_packet_override");

	@CodecField
	private int windowId;
	@CodecField
	private int stateId;
	@CodecField
	private int slotId;
	@CodecField
	private int usedButton;
	@CodecField
	private ItemStack clickedItem;
	@CodecField
	private String mode;
	private Int2ObjectMap<ItemStack> changedSlots;

    public ServerboundContainerClickPacketOverride() {
		super(ID);
    }

    public ServerboundContainerClickPacketOverride(int windowId, int stateId, int slotId, int usedButton, ClickType mode, ItemStack clickedItem, Int2ObjectMap<ItemStack> changedSlots) {
		super(ID);
        this.windowId = windowId;
		this.stateId = stateId;
		this.slotId = slotId;
		this.usedButton = usedButton;
		this.clickedItem = clickedItem != null ? clickedItem.copy() : null;
		this.mode = mode.name();
		this.changedSlots = changedSlots;
    }

	@Override
	public void encode(FriendlyByteBuf output) {
		super.encode(output);
		output.writeMap(this.changedSlots, FriendlyByteBuf::writeInt, FriendlyByteBuf::writeItem);
	}

	@Override
	public void decode(FriendlyByteBuf input) {
		super.decode(input);
		IntFunction<Int2ObjectOpenHashMap<ItemStack>> intfunction = FriendlyByteBuf.limitValue(Int2ObjectOpenHashMap::new, 128);
		this.changedSlots = Int2ObjectMaps.unmodifiable(input.readMap(intfunction, FriendlyByteBuf::readInt, FriendlyByteBuf::readItem));
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
				boolean flag = this.stateId != player.containerMenu.getStateId();
				player.containerMenu.suppressRemoteUpdates();
				player.containerMenu.clicked(slotId, usedButton, ClickType.valueOf(mode), player);

				for(Int2ObjectMap.Entry<ItemStack> entry : Int2ObjectMaps.fastIterable(changedSlots)) {
					player.containerMenu.setRemoteSlotNoCopy(entry.getIntKey(), entry.getValue());
				}

				player.containerMenu.setRemoteCarried(clickedItem);
				player.containerMenu.resumeRemoteUpdates();
				if (flag) {
					// Original: player.containerMenu.broadcastFullState();

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
	}

}
