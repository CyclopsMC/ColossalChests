package org.cyclops.colossalchests.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.RegistryEntries;
import org.cyclops.cyclopscore.config.extendedconfig.TileEntityConfig;

/**
 * Config for the {@link TileUncolossalChest}.
 * @author rubensworks
 *
 */
public class TileUncolossalChestConfig extends TileEntityConfig<TileUncolossalChest> {

    public TileUncolossalChestConfig() {
        super(
                ColossalChests._instance,
                "uncolossal_chest",
                (eConfig) -> new TileEntityType<>(TileUncolossalChest::new,
                        Sets.newHashSet(RegistryEntries.BLOCK_UNCOLOSSAL_CHEST), null)
        );
    }

}
