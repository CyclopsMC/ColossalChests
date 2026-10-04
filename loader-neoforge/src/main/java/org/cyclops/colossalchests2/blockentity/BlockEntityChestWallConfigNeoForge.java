package org.cyclops.colossalchests2.blockentity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.cyclops.colossalchests2.capability.ItemHandlerChestStorage;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * NeoForge config for the {@link BlockEntityChestWall}, exposing item handlers on formed functional walls.
 * @author rubensworks
 */
public class BlockEntityChestWallConfigNeoForge<M extends ModBase<?>> extends BlockEntityChestWallConfig<M> {

    public BlockEntityChestWallConfigNeoForge(M mod) {
        super(mod, BlockEntityChestWall::new);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    protected void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, getInstance(),
                (wall, side) -> wall.getItemHandlerLogic().map(ItemHandlerChestStorage::new).orElse(null));
    }
}
