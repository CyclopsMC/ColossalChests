package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerFilteredInterface}.
 * @author rubensworks
 */
public class ContainerFilteredInterfaceConfig<M extends IModBase> extends GuiConfigCommon<ContainerFilteredInterface, M> {

    public ContainerFilteredInterfaceConfig(M mod) {
        super(mod, "filtered_interface", eConfig -> new ContainerTypeDataCommon<>(ContainerFilteredInterface::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerFilteredInterface> getScreenFactoryProvider() {
        return new ContainerFilteredInterfaceConfigScreenFactoryProvider();
    }
}
