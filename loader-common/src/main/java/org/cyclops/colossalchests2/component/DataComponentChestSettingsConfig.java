package org.cyclops.colossalchests2.component;

import org.cyclops.colossalchests2.inventory.ChestSettings;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the data component that keeps a core's GUI settings on its item.
 * @author rubensworks
 */
public class DataComponentChestSettingsConfig<M extends IModBase> extends DataComponentConfigCommon<ChestSettings, M> {

    public DataComponentChestSettingsConfig(M mod) {
        super(mod, "chest_settings", builder -> builder
                .persistent(ChestSettings.CODEC)
                .networkSynchronized(ChestSettings.STREAM_CODEC.cast()));
    }

}
