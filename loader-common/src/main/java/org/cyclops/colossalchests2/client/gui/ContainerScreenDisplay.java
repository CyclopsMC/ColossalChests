package org.cyclops.colossalchests2.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;

import java.util.List;

/**
 * Settings of a Display wall.
 * @author rubensworks
 */
public class ContainerScreenDisplay extends AbstractContainerScreen<ContainerDisplay> {

    public ContainerScreenDisplay(ContainerDisplay menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ContainerDisplay.WIDTH;
        this.imageHeight = ContainerDisplay.HEIGHT;
        this.inventoryLabelY = ContainerDisplay.INVENTORY_Y - 11;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GuiPanels.drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            GuiPanels.drawSlot(guiGraphics, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && menu.isGhostSlot(hoveredSlot.index) && !hoveredSlot.hasItem()) {
            String key = "gui.colossalchests2.wall.displayed";
            guiGraphics.renderComponentTooltip(font, List.of(Component.translatable(key),
                    Component.translatable(key + ".info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
