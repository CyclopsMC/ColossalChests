package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.block.ChestMaterial;
import org.cyclops.cyclopscore.RegistryEntries;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;

import java.util.stream.Collectors;

/**
 * Config for the {@link TileInterface}.
 * @author rubensworks
 *
 */
public class TileInterfaceConfig extends TileEntityConfig<TileInterface> {

    public TileInterfaceConfig() {
        super(
                ColossalChests._instance,
                "interface",
                (eConfig) -> new TileEntityType<>(TileInterface::new,
                        ChestMaterial.VALUES.stream()
                                .map(ChestMaterial::getBlockInterface)
                                .collect(Collectors.toSet()), null)
        );
    }

}
