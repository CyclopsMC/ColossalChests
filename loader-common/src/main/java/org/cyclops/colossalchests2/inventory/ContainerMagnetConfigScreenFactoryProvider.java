package org.cyclops.colossalchests2.inventory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.colossalchests2.client.gui.ContainerScreenMagnet;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;

/**
 * Only loaded on the client.
 * @author rubensworks
 */
public class ContainerMagnetConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerMagnet> {
    @Override
    public <U extends Screen & MenuAccess<ContainerMagnet>> MenuScreens.ScreenConstructor<ContainerMagnet, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMagnet::new);
    }
}
