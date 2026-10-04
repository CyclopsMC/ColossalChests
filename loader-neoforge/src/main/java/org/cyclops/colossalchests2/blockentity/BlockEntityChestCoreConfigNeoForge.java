package org.cyclops.colossalchests2.blockentity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorage;
import org.cyclops.colossalchests2.client.render.RenderChestCoreNeoForge;
import org.cyclops.colossalchests2.network.ChestNetwork;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * NeoForge config for the {@link BlockEntityChestCore}, exposing item handlers on formed cores.
 * @author rubensworks
 */
public class BlockEntityChestCoreConfigNeoForge<M extends ModBase<?>> extends BlockEntityChestCoreConfig<M> {

    public BlockEntityChestCoreConfigNeoForge(M mod) {
        super(mod, BlockEntityChestCore::new);
        mod.getModEventBus().addListener(this::registerCapabilities);
        BlockEntityChestCore.capabilityInvalidator = Level::invalidateCapabilities;
        ChestNetwork.canReceive = (player, packet) -> player.connection.hasChannel(packet);
    }

    @Override
    protected BlockEntityRendererProvider<BlockEntityChestCore> getRendererProvider() {
        return RenderChestCoreNeoForge::new;
    }

    protected void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, getInstance(),
                (core, side) -> core.isFormed() ? new ItemHandlerChestStorage(core.getItemHandlerLogic()) : null);
    }
}
