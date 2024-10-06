package org.cyclops.colossalchests.inventory.container;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigCommon;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;
import org.cyclops.cyclopscore.init.IModBase;

/**
 * Config for {@link ContainerUncolossalChest}.
 * @author rubensworks
 */
public class ContainerUncolossalChestConfig<M extends IModBase> extends GuiConfigCommon<ContainerUncolossalChest, M> {

    public ContainerUncolossalChestConfig(M mod) {
        super(mod,
                "uncolossal_chest",
                eConfig -> new MenuType<>(ContainerUncolossalChest::new, FeatureFlags.VANILLA_SET));
    }

    @Override
    public GuiConfigScreenFactoryProvider<ContainerUncolossalChest> getScreenFactoryProvider() {
        return new ContainerUncolossalChestConfigScreenFactoryProvider();
    }
}
