package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerDisplay}.
 * @author rubensworks
 */
public class ContainerDisplayConfig<M extends IModBase> extends GuiConfigCommon<ContainerDisplay, M> {

    public ContainerDisplayConfig(M mod) {
        super(mod, "display", eConfig -> new ContainerTypeDataCommon<>(ContainerDisplay::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerDisplay> getScreenFactoryProvider() {
        return new ContainerDisplayConfigScreenFactoryProvider();
    }
}
