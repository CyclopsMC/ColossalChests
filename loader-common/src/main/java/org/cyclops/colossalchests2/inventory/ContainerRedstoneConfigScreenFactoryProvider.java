package org.cyclops.colossalchests2.inventory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.colossalchests2.client.gui.ContainerScreenRedstone;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;

/**
 * Only loaded on the client.
 * @author rubensworks
 */
public class ContainerRedstoneConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerRedstone> {
    @Override
    public <U extends Screen & MenuAccess<ContainerRedstone>> MenuScreens.ScreenConstructor<ContainerRedstone, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenRedstone::new);
    }
}
