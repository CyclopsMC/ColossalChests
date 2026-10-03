package org.cyclops.colossalchests2.modcompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.colossalchests2.ColossalChests;
import org.cyclops.colossalchests2.Reference;
import org.cyclops.colossalchests2.RegistryEntries;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.commoncapabilities.api.capability.Capabilities;
import org.cyclops.commoncapabilities.api.capability.inventorystate.IInventoryState;
import org.cyclops.cyclopscore.modcompat.ICompatInitializer;
import org.cyclops.cyclopscore.modcompat.IModCompat;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes the inventory state of chests through Common Capabilities, so consumers can skip rescanning unchanged chests.
 * @author rubensworks
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
        return "If the inventory state capability should be exposed on chests.";
    }

    @Override
    public ICompatInitializer createInitializer() {
        return () -> ColossalChests._instance.getModEventBus().addListener(CommonCapabilitiesModCompat::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.InventoryState.BLOCK, RegistryEntries.BLOCK_ENTITY_CHEST_CORE.value(),
                (core, side) -> core.isFormed() ? new InventoryStateChestStorage(core.getStorage()) : null);
        if (BlockChestWall.EXPOSES_CAPABILITIES) {
            event.registerBlock(Capabilities.InventoryState.BLOCK, CommonCapabilitiesModCompat::getWallInventoryState,
                    BlockChestWall.getInstances().toArray(Block[]::new));
        }
    }

    @Nullable
    private static IInventoryState getWallInventoryState(Level level, BlockPos pos, BlockState state,
                                                         @Nullable BlockEntity blockEntity, @Nullable Direction side) {
        if (!state.getValue(BlockChestWall.FORMED)) {
            return null;
        }
        return ChestCoreIndex.findFormedCore(level, pos)
                .map(core -> (IInventoryState) new InventoryStateChestStorage(core.getStorage()))
                .orElse(null);
    }
}
