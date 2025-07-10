package org.cyclops.colossalchests.block;

import org.cyclops.colossalchests.item.ItemBlockMaterial;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for the {@link Interface}.
 * @author rubensworks
 *
 */
public class InterfaceConfigForge<M extends IModBase> extends InterfaceConfig<M> {

    public InterfaceConfigForge(M mod, ChestMaterial material) {
        super(
                mod,
            "interface_" + material.getName(),
                (eConfig, properties) -> new InterfaceForge(((InterfaceConfig<M>) eConfig).getProperties(), material),
                ItemBlockMaterial.getItemConstructor(material, "interface")
        );
    }

}
