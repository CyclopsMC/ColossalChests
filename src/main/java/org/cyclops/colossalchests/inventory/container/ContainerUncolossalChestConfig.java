package org.cyclops.colossalchests.inventory.container;

import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.gui.container.ContainerScreenUncolossalChest;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;

/**
 * Config for {@link ContainerUncolossalChest}.
 * @author rubensworks
 */
public class ContainerUncolossalChestConfig extends GuiConfig<ContainerUncolossalChest> {

    public ContainerUncolossalChestConfig() {
        super(ColossalChests._instance,
                "uncolossal_chest",
                eConfig -> new MenuType<>(ContainerUncolossalChest::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & MenuAccess<ContainerUncolossalChest>> MenuScreens.ScreenConstructor<ContainerUncolossalChest, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenUncolossalChest::new);
    }

}
