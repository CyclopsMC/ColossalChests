package org.cyclops.colossalchests.blockentity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.cyclops.cyclopscore.init.ModBase;

/**
 * @author rubensworks
 */
public class BlockEntityInterfaceConfigNeoForge<M extends ModBase> extends BlockEntityInterfaceConfig<M> {
    public BlockEntityInterfaceConfigNeoForge(M mod) {
        super(mod, BlockEntityInterface::new);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                getInstance(),
                (blockEntity, context) -> {
                    BlockEntityColossalChest core = blockEntity.getCore();
                    if (core != null) {
                        return new InvWrapper(core.getInventory());
                    }
                    return null;
                }
        );
    }
}
