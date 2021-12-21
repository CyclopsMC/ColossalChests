package org.cyclops.colossalchests.item;

import net.minecraft.item.Item;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemUpgradeToolConfig extends ItemConfig {

    public ItemUpgradeToolConfig(boolean upgrade) {
        super(
                ColossalChests._instance,
                "upgrade_tool" + (upgrade ? "" : "_reverse"),
                (eConfig) -> new ItemUpgradeTool(new Item.Properties()
                        .stacksTo(1)
                        .tab(ColossalChests._instance.getDefaultItemGroup()),
                        upgrade)
        );
    }

}
