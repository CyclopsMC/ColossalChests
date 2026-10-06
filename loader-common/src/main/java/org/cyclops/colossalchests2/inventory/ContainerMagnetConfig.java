package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerMagnet}.
 * @author rubensworks
 */
public class ContainerMagnetConfig<M extends IModBase> extends GuiConfigCommon<ContainerMagnet, M> {

    public ContainerMagnetConfig(M mod) {
        super(mod, "magnet", eConfig -> new ContainerTypeDataCommon<>(ContainerMagnet::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerMagnet> getScreenFactoryProvider() {
        return new ContainerMagnetConfigScreenFactoryProvider();
    }
}
