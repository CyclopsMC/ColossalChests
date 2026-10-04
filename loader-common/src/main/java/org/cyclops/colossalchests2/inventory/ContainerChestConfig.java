package org.cyclops.colossalchests2.inventory;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerChest}.
 * @author rubensworks
 */
public class ContainerChestConfig<M extends IModBase> extends GuiConfigCommon<ContainerChest, M> {

    public ContainerChestConfig(M mod) {
        super(mod, "chest", eConfig -> new ContainerTypeDataCommon<>(ContainerChest::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerChest> getScreenFactoryProvider() {
        return new ContainerChestConfigScreenFactoryProvider();
    }
}
