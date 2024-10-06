package org.cyclops.colossalchests.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeDataCommon;

/**
 * Config for {@link ContainerColossalChest}.
 * @author rubensworks
 */
public class ContainerColossalChestConfig<M extends IModBase> extends GuiConfigCommon<ContainerColossalChest, M> {

    public ContainerColossalChestConfig(M mod) {
        super(mod,
                "colossal_chest",
                eConfig -> new ContainerTypeDataCommon<>(ContainerColossalChest::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerColossalChest> getScreenFactoryProvider() {
        return new ContainerColossalChestConfigScreenFactoryProvider();
    }
}
