package org.cyclops.colossalchests.modcompat;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.cyclops.colossalchests.Capabilities;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.tileentity.TileColossalChest;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.cyclopscore.datastructure.Wrapper;
import org.cyclops.cyclopscore.inventory.IndexedSlotlessItemHandlerWrapper;
import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.cyclopscore.modcompat.capabilities.CapabilityConstructorRegistry;
import org.cyclops.cyclopscore.modcompat.capabilities.DefaultCapabilityProvider;
import org.cyclops.cyclopscore.modcompat.capabilities.SimpleCapabilityConstructor;

import javax.annotation.Nullable;

/**
 * Mod compat for the Common Capabilities mod.
 * @author rubensworks
 *
 */
public class CommonCapabilitiesModCompat implements IModCompat {

	@Override
	public String getId() {
		return Reference.MOD_COMMONCAPABILITIES;
	}

	@Override
	public boolean isEnabledDefault() {
		return true;
	}

	@Override
	public String getComment() {
		return "If slotless item handlers should be registered to colossal chests.";
	}

	@Override
	public ICompatInitializer createInitializer() {
		return () -> {
			CapabilityConstructorRegistry registry = ColossalChests._instance.getCapabilityConstructorRegistry();
			// Worker
			registry.registerTile(TileColossalChest.class,
					new SimpleCapabilityConstructor<ISlotlessItemHandler, TileColossalChest>() {
						@Override
						public Capability<ISlotlessItemHandler> getCapability() {
							return Capabilities.SLOTLESS_ITEMHANDLER;
						}

						@Nullable
						@Override
						public ICapabilityProvider createProvider(TileColossalChest host) {
							// Wrapper hack because Java otherwise complains that the optionalSlotlessItemHandler may not be initialized yet, and it won't compile.
							Wrapper<LazyOptional<ISlotlessItemHandler>> optionalSlotlessItemHandler = new Wrapper<>();
							optionalSlotlessItemHandler.set(LazyOptional.of(() -> {
								LazyOptional<IItemHandler> optionalItemHandler = host.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
								optionalItemHandler.addListener((o) -> optionalSlotlessItemHandler.get().invalidate());
								return new IndexedSlotlessItemHandlerWrapper(optionalItemHandler.orElse(null),
										(IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) host.getInventory());
							}));
							return new DefaultCapabilityProvider<>(this::getCapability, optionalSlotlessItemHandler.get());
						}
					});
		};
	}

}
