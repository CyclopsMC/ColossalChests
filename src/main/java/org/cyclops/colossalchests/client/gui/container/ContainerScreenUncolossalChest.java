package org.cyclops.colossalchests.client.gui.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.colossalchests.inventory.container.ContainerUncolossalChest;
import org.cyclops.cyclopscore.client.gui.container.ContainerScreenExtended;

/**
 * @author rubensworks
 */
public class ContainerScreenUncolossalChest extends ContainerScreenExtended<ContainerUncolossalChest> {
    public ContainerScreenUncolossalChest(ContainerUncolossalChest container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);
        font.drawString(getTitle().getFormattedText(), 8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation("textures/gui/container/hopper.png");
    }
}
