package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerRedstone}.
 * @author rubensworks
 */
public class ContainerRedstoneConfig<M extends IModBase> extends GuiConfigCommon<ContainerRedstone, M> {

    public ContainerRedstoneConfig(M mod) {
        super(mod, "redstone", eConfig -> new ContainerTypeDataCommon<>(ContainerRedstone::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerRedstone> getScreenFactoryProvider() {
        return new ContainerRedstoneConfigScreenFactoryProvider();
    }
}
