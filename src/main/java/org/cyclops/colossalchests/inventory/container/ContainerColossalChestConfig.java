package org.cyclops.colossalchests.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.IContainerFactory;
import org.cyclops.colossalchests.ColossalChests;
import org.cyclops.colossalchests.client.gui.container.ContainerScreenColossalChest;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;

/**
 * Config for {@link ContainerColossalChest}.
 * @author rubensworks
 */
public class ContainerColossalChestConfig extends GuiConfig<ContainerColossalChest> {

    public ContainerColossalChestConfig() {
        super(ColossalChests._instance,
                "colossal_chest",
                eConfig -> new ContainerType<>((IContainerFactory<ContainerColossalChest>) ContainerColossalChest::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerColossalChest>> ScreenManager.IScreenFactory<ContainerColossalChest, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenColossalChest::new);
    }

}
