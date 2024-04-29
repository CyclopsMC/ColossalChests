package org.cyclops.colossalchests.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.gui.container.ContainerScreenColossalChest;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;

/**
 * Config for {@link ContainerColossalChest}.
 * @author rubensworks
 */
public class ContainerColossalChestConfig extends GuiConfig<ContainerColossalChest> {

    public ContainerColossalChestConfig() {
        super(ColossalChests._instance,
                "colossal_chest",
                eConfig -> new ContainerTypeData<>(ContainerColossalChest::new, FeatureFlags.VANILLA_SET));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerColossalChest>> MenuScreens.ScreenConstructor<ContainerColossalChest, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenColossalChest::new);
    }

}
