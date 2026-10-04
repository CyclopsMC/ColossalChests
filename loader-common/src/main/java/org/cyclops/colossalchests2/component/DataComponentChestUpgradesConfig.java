package org.cyclops.colossalchests2.component;

import net.minecraft.world.item.component.ItemContainerContents;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the upgrades a core item carries.
 * @author rubensworks
 */
public class DataComponentChestUpgradesConfig<M extends IModBase> extends DataComponentConfigCommon<ItemContainerContents, M> {

    public DataComponentChestUpgradesConfig(M mod) {
        super(mod, "chest_upgrades", builder -> builder
                .persistent(ItemContainerContents.CODEC)
                .networkSynchronized(ItemContainerContents.STREAM_CODEC));
    }

}
