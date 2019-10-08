package org.cyclops.colossalchests.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
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
                eConfig -> new ContainerType<>(ContainerUncolossalChest::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerUncolossalChest>> ScreenManager.IScreenFactory<ContainerUncolossalChest, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenUncolossalChest::new);
    }

}
