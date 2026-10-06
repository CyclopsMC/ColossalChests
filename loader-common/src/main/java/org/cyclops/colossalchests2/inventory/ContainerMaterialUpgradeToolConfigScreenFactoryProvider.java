package org.cyclops.colossalchests2.inventory;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import org.cyclops.colossalchests2.client.gui.ContainerScreenMaterialUpgradeTool;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfigScreenFactoryProvider;

/**
 * Only loaded on the client.
 * @author rubensworks
 */
public class ContainerMaterialUpgradeToolConfigScreenFactoryProvider extends GuiConfigScreenFactoryProvider<ContainerMaterialUpgradeTool> {
    @Override
    public <U extends Screen & MenuAccess<ContainerMaterialUpgradeTool>> MenuScreens.ScreenConstructor<ContainerMaterialUpgradeTool, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenMaterialUpgradeTool::new);
    }
}
