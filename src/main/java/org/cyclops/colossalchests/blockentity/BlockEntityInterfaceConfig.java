package org.cyclops.colossalchests.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
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
    }

}
