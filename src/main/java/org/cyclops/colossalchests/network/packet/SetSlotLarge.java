package org.cyclops.colossalchests.network.packet;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.network.CodecField;
import org.cyclops.cyclopscore.network.PacketCodec;

/**
 * Packet for setting slots with id's larger than max short size (65535).
 * {@link SSetSlotPacket}.
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
	@OnlyIn(Dist.CLIENT)
	public void actionClient(World world, PlayerEntity player) {
		// Modified code from NetHandlerPlayClient#handleSetSlot
		if (windowId == player.containerMenu.containerId) {
			player.containerMenu.setItem(slot, itemStack);
		}
	}

	@Override
	public void actionServer(World world, ServerPlayerEntity player) {

	}
	
}