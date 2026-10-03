package org.cyclops.colossalchests2.component;

import net.minecraft.network.codec.ByteBufCodecs;
import org.cyclops.colossalchests2.storage.ChestStorage;
import org.cyclops.cyclopscore.config.extendedconfig.DataComponentConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Data component holding the contents of a broken chest core.
 * @author rubensworks
 */
public class DataComponentChestContentsConfig<M extends IModBase> extends DataComponentConfigCommon<ChestStorage.Contents, M> {

    public DataComponentChestContentsConfig(M mod) {
        super(mod, "chest_contents", builder -> builder
                .persistent(ChestStorage.CODEC)
                .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(ChestStorage.CODEC)));
    }

}
