package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerInterface}.
 * @author rubensworks
 */
public class ContainerInterfaceConfig<M extends IModBase> extends GuiConfigCommon<ContainerInterface, M> {

    public ContainerInterfaceConfig(M mod) {
        super(mod, "interface", eConfig -> new ContainerTypeDataCommon<>(ContainerInterface::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerInterface> getScreenFactoryProvider() {
        return new ContainerInterfaceConfigScreenFactoryProvider();
    }
}
