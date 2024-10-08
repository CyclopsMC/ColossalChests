package org.cyclops.colossalchests.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.BlockEntityConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

import java.util.stream.Collectors;

/**
 * Config for the {@link BlockEntityInterface}.
 * @author rubensworks
 *
 */
public class BlockEntityInterfaceConfig<M extends IModBase> extends BlockEntityConfigCommon<BlockEntityInterface, M> {

    public BlockEntityInterfaceConfig(M mod, BlockEntityType.BlockEntitySupplier<? extends BlockEntityInterface> blockEntitySupplier) {
        super(
                mod,
                "interface",
                (eConfig) -> new BlockEntityType<>(blockEntitySupplier,
                        ChestMaterial.VALUES.stream()
                                .map(ChestMaterial::getBlockInterface)
                                .collect(Collectors.toSet()), null)
        );
    }

}
