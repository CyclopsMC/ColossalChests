package org.cyclops.colossalchests2.inventory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.colossalchests2.client.gui.ContainerScreenDisplay;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;

/**
 * Only loaded on the client.
 * @author rubensworks
 */
public class ContainerDisplayConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerDisplay> {
    @Override
    public <U extends Screen & MenuAccess<ContainerDisplay>> MenuScreens.ScreenConstructor<ContainerDisplay, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenDisplay::new);
    }
}
