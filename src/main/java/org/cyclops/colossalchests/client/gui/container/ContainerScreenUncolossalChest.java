package org.cyclops.colossalchests.client.gui.container;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
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
    protected void renderLabels(PoseStack matrixStack, int p_146979_1_, int p_146979_2_) {
        //super.drawGuiContainerForegroundLayer(matrixStack, p_146979_1_, p_146979_2_);
        font.draw(matrixStack, getTitle().getString(), 8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation("textures/gui/container/hopper.png");
    }
}
