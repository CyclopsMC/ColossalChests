package org.cyclops.colossalchests.item;

import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the facade.
 * @author rubensworks
 */
public class ItemUpgradeToolConfig<M extends IModBase> extends ItemConfigCommon<M> {

    public ItemUpgradeToolConfig(M mod, boolean upgrade) {
        super(
                mod,
                "upgrade_tool" + (upgrade ? "" : "_reverse"),
                (eConfig, properties) -> new ItemUpgradeTool(properties
                        .stacksTo(1),
                        upgrade)
        );
    }

}
