package org.cyclops.colossalchests2.material;

import net.minecraft.world.item.Item;
import org.cyclops.colossalchests2.block.ChestMaterial;
import org.cyclops.cyclopscore.config.extendedconfig.ItemConfigCommon;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for a Material Upgrade item.
 * @author rubensworks
 */
public class ItemMaterialUpgradeConfig<M extends IModBase> extends ItemConfigCommon<M> {

    public ItemMaterialUpgradeConfig(M mod, ChestMaterial to) {
        super(mod, "material_upgrade_" + to.getName(), eConfig -> new ItemMaterialUpgrade(new Item.Properties().stacksTo(1), to));
    }

}
