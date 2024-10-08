package org.cyclops.colossalchests.block;

import net.minecraft.world.item.Item;
import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link Interface}.
 * @author rubensworks
 *
 */
public class InterfaceConfigFabric<M extends IModBase> extends InterfaceConfig<M> {

    public InterfaceConfigFabric(M mod, ChestMaterial material) {
        super(
                mod,
            "interface_" + material.getName(),
                eConfig -> new InterfaceFabric(((InterfaceConfig<M>) eConfig).getProperties(), material),
                (eConfig, block) -> new ItemBlockMaterial(block, new Item.Properties(), material)
        );
    }

}
