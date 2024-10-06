package org.cyclops.colossalchests.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.colossalchests.client.gui.container.ContainerScreenColossalChest;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;

/**
 * @author rubensworks
 */
public class ContainerColossalChestConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerColossalChest> {
    @Override
    public <U extends Screen & MenuAccess<ContainerColossalChest>> MenuScreens.ScreenConstructor<ContainerColossalChest, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenColossalChest::new);
    }
}
