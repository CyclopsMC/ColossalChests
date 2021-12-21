package org.cyclops.colossalchests.client.gui.container;

import com.mojang.blaze3d.matrix.MatrixStack;
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
    protected void renderLabels(MatrixStack matrixStack, int p_146979_1_, int p_146979_2_) {
        //super.drawGuiContainerForegroundLayer(matrixStack, p_146979_1_, p_146979_2_);
        font.draw(matrixStack, getTitle().getString(), 8 + offsetX, 6 + offsetY, 4210752);
    }

    @Override
    protected void renderBg(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        // TODO: rm
    }

    @Override
    protected ResourceLocation constructGuiTexture() {
        return new ResourceLocation("textures/gui/container/hopper.png");
    }
}
