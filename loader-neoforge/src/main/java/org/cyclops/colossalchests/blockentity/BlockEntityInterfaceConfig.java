package org.cyclops.colossalchests.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfig;

import java.util.stream.Collectors;

/**
 * Config for the {@link BlockEntityInterface}.
 * @author rubensworks
 *
 */
public class BlockEntityInterfaceConfig extends BlockEntityConfig<BlockEntityInterface> {

    public BlockEntityInterfaceConfig() {
        super(
                ColossalChests._instance,
                "interface",
                (eConfig) -> new BlockEntityType<>(BlockEntityInterface::new,
                        ChestMaterial.VALUES.stream()
                                .map(ChestMaterial::getBlockInterface)
                                .collect(Collectors.toSet()), null)
        );
        ColossalChests._instance.getModEventBus().addListener(this::registerCapabilities);
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
