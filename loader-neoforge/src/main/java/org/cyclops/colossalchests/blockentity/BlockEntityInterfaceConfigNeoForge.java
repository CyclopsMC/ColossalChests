package org.cyclops.colossalchests.blockentity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import org.cyclops.cyclopscore.init.ModBaseNeoForge;

/**
 * @author rubensworks
 */
public class BlockEntityInterfaceConfigNeoForge<M extends ModBaseNeoForge<?>> extends BlockEntityInterfaceConfig<M> {
    public BlockEntityInterfaceConfigNeoForge(M mod) {
        super(mod, BlockEntityInterface::new);
        mod.getModEventBus().addListener(this::registerCapabilities);
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                getInstance(),
                (blockEntity, context) -> {
                    BlockEntityColossalChest core = blockEntity.getCore();
                    if (core != null) {
                        return VanillaContainerWrapper.of(core.getInventory());
                    }
                    return null;
                }
        );
    }
}
