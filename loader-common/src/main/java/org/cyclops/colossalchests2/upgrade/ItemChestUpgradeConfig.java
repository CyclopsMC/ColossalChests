package org.cyclops.colossalchests2.upgrade;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for an upgrade item.
 * @author rubensworks
 */
public class ItemChestUpgradeConfig<M extends IModBase> extends ItemConfigCommon<M> {

    public ItemChestUpgradeConfig(M mod, ChestUpgrade upgrade) {
        super(mod, "upgrade_" + upgrade.getId().getPath(), eConfig -> new ItemChestUpgrade(new Item.Properties().stacksTo(16), upgrade));
    }

}
