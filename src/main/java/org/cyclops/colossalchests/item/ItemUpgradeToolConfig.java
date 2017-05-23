package org.cyclops.colossalchests.item;

import net.minecraft.item.ItemStack;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfig;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemUpgradeToolConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemUpgradeToolConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemUpgradeToolConfig() {
        super(
                ColossalChests._instance,
                true,
                "upgradeTool",
                null,
                ItemUpgradeTool.class
        );
    }

    @Override
    public String getModelName(ItemStack itemStack) {
        return itemStack.getMetadata() == 0 ? super.getModelName(itemStack) : "downgradeTool";
    }

}
