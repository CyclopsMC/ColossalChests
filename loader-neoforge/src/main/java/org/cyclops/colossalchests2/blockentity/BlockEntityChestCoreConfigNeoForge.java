package org.cyclops.colossalchests2.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.cyclops.colossalchests2.block.BlockChestWall;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorage;
import org.cyclops.colossalchests2.client.render.RenderChestCoreNeoForge;
import org.cyclops.colossalchests2.multiblock.ChestCoreIndex;
import org.cyclops.cyclopscore.init.ModBase;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge config for the {@link BlockEntityChestCore}, exposing item handlers on formed cores and walls.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfigNeoForge<M extends ModBase<?>> extends BlockEntityChestCoreConfig<M> {

    public BlockEntityChestCoreConfigNeoForge(M mod) {
        super(mod, BlockEntityChestCore::new);
        mod.getModEventBus().addListener(this::registerCapabilities);
        BlockEntityChestCore.capabilityInvalidator = Level::invalidateCapabilities;
    }

    @Override
    protected BlockEntityRendererProvider<BlockEntityChestCore> getRendererProvider() {
        return RenderChestCoreNeoForge::new;
    }

    protected void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, getInstance(),
                (core, side) -> core.isFormed() ? new ItemHandlerChestStorage(core.getItemHandlerLogic()) : null);
        if (BlockChestWall.EXPOSES_CAPABILITIES) {
            event.registerBlock(Capabilities.ItemHandler.BLOCK, BlockEntityChestCoreConfigNeoForge::getWallItemHandler,
                    BlockChestWall.getInstances().toArray(Block[]::new));
        }
    }

    @Nullable
    private static IItemHandler getWallItemHandler(Level level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state,
                                                   @Nullable net.minecraft.world.level.block.entity.BlockEntity blockEntity, @Nullable Direction side) {
        if (!state.getValue(BlockChestWall.FORMED)) {
            return null;
        }
        return ChestCoreIndex.findFormedCore(level, pos)
                .map(core -> (IItemHandler) new ItemHandlerChestStorage(core.getItemHandlerLogic()))
                .orElse(null);
    }
}
