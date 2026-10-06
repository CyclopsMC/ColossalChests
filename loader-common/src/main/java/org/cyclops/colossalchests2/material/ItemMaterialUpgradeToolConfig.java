package org.cyclops.colossalchests2.material;

import net.minecraft.world.item.Item;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link ItemMaterialUpgradeTool}.
 * @author rubensworks
 */
public class ItemMaterialUpgradeToolConfig<M extends IModBase> extends ItemConfigCommon<M> {

    public ItemMaterialUpgradeToolConfig(M mod) {
        super(mod, "material_upgrade_tool", eConfig -> new ItemMaterialUpgradeTool(new Item.Properties().stacksTo(1)));
    }

}
