package org.cyclops.colossalchests.inventory.container;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.colossalchests.client.gui.container.ContainerScreenUncolossalChest;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;

/**
 * @author rubensworks
 */
public class ContainerUncolossalChestConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerUncolossalChest> {
    @Override
    public <U extends Screen & MenuAccess<ContainerUncolossalChest>> MenuScreens.ScreenConstructor<ContainerUncolossalChest, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenUncolossalChest::new);
    }
}
