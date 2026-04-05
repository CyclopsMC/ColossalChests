package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;

/**
 * @author rubensworks
 */
public class ContainerScreenUncolossalChest extends ContainerScreenExtended<ContainerUncolossalChest> {
    public ContainerScreenUncolossalChest(ContainerUncolossalChest container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphicsExtractor, int p_146979_1_, int p_146979_2_) {
        //super.drawGuiContainerForegroundLayer(matrixStack, p_146979_1_, p_146979_2_);
        guiGraphicsExtractor.text(this.font, getTitle().getString(), 8 + offsetX, 6 + offsetY, ARGB.opaque(4210752), false);
    }

    @Override
    protected Identifier constructGuiTexture() {
        return Identifier.parse("textures/gui/container/hopper.png");
    }
}
