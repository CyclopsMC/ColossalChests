package org.cyclops.colossalchests.modcompat;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BaseCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.Reference;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.colossalchests.blockentity.BlockEntityColossalChest;
import org.cyclops.colossalchests.blockentity.BlockEntityInterface;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.commoncapabilities.api.capability.itemhandler.ISlotlessItemHandler;
import org.cyclops.cyclopscore.inventory.IndexedSlotlessItemHandlerWrapper;
import org.cyclops.cyclopscore.inventory.SimpleInventory;
import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.cyclops.cyclopscore.modcompat.capabilities.CapabilityConstructorRegistry;
import org.cyclops.cyclopscore.modcompat.capabilities.ICapabilityConstructor;

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

            // Slotless item handler
            registry.registerBlockEntity(RegistryEntries.BLOCK_ENTITY_COLOSSAL_CHEST::value,
                    new ICapabilityConstructor<BlockEntityColossalChest, Direction, ISlotlessItemHandler, BlockEntityType<BlockEntityColossalChest>>() {
                        @Override
                        public BaseCapability<ISlotlessItemHandler, Direction> getCapability() {
                            return org.cyclops.commoncapabilities.api.capability.Capabilities.SlotlessItemHandler.BLOCK;
                        }

                        @Override
                        public ICapabilityProvider<BlockEntityColossalChest, Direction, ISlotlessItemHandler> createProvider(BlockEntityType<BlockEntityColossalChest> capabilityKey) {
                            return (blockEntity, side) -> new IndexedSlotlessItemHandlerWrapper(new InvWrapper(blockEntity.getInventory()),
                                    (IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) blockEntity.getInventory());
                        }
                    });
            registry.registerBlockEntity(RegistryEntries.BLOCK_ENTITY_INTERFACE::value,
                    new ICapabilityConstructor<BlockEntityInterface, Direction, ISlotlessItemHandler, BlockEntityType<BlockEntityInterface>>() {
                        @Override
                        public BaseCapability<ISlotlessItemHandler, Direction> getCapability() {
                            return org.cyclops.commoncapabilities.api.capability.Capabilities.SlotlessItemHandler.BLOCK;
                        }

                        @Override
                        public ICapabilityProvider<BlockEntityInterface, Direction, ISlotlessItemHandler> createProvider(BlockEntityType<BlockEntityInterface> capabilityKey) {
                            return (blockEntity, side) -> {
                                BlockEntityColossalChest core = blockEntity.getCore();
                                if (core != null) {
                                    return new IndexedSlotlessItemHandlerWrapper(new InvWrapper(core.getInventory()),
                                            (IndexedSlotlessItemHandlerWrapper.IInventoryIndexReference) core.getInventory());
                                }
                                return null;
                            };
                        }
                    });

            // Inventory state
            registry.registerBlockEntity(RegistryEntries.BLOCK_ENTITY_COLOSSAL_CHEST::value,
                    new ICapabilityConstructor<BlockEntityColossalChest, Direction, IInventoryState, BlockEntityType<BlockEntityColossalChest>>() {
                        @Override
                        public BaseCapability<IInventoryState, Direction> getCapability() {
                            return org.cyclops.commoncapabilities.api.capability.Capabilities.InventoryState.BLOCK;
                        }

                        @Override
                        public ICapabilityProvider<BlockEntityColossalChest, Direction, IInventoryState> createProvider(BlockEntityType<BlockEntityColossalChest> capabilityKey) {
                            return (blockEntity, side) -> () -> ((SimpleInventory) blockEntity.getInventory()).getState();
                        }
                    });
            registry.registerBlockEntity(RegistryEntries.BLOCK_ENTITY_INTERFACE::value,
                    new ICapabilityConstructor<BlockEntityInterface, Direction, IInventoryState, BlockEntityType<BlockEntityInterface>>() {
                        @Override
                        public BaseCapability<IInventoryState, Direction> getCapability() {
                            return org.cyclops.commoncapabilities.api.capability.Capabilities.InventoryState.BLOCK;
                        }

                        @Override
                        public ICapabilityProvider<BlockEntityInterface, Direction, IInventoryState> createProvider(BlockEntityType<BlockEntityInterface> capabilityKey) {
                            return (blockEntity, side) -> {
                                BlockEntityColossalChest core = blockEntity.getCore();
                                if (core != null) {
                                    return () -> ((SimpleInventory) core.getInventory()).getState();
                                }
                                return null;
                            };
                        }
                    });
        };
    }

}
