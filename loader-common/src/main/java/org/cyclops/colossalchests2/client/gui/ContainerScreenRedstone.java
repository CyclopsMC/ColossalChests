package org.cyclops.colossalchests2.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.cyclops.colossalchests2.inventory.ContainerRedstone;

import java.util.List;

/**
 * Settings of a Redstone wall.
 * @author rubensworks
 */
public class ContainerScreenRedstone extends AbstractContainerScreen<ContainerRedstone> {

    private static final int COLOR_LABEL = 0x404040;
    private static final int COLOR_SIGNAL = 0xC02010;

    public ContainerScreenRedstone(ContainerRedstone menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ContainerRedstone.WIDTH;
        this.imageHeight = ContainerRedstone.HEIGHT;
        this.inventoryLabelY = ContainerRedstone.INVENTORY_Y - 11;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GuiPanels.drawPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        for (Slot slot : menu.slots) {
            GuiPanels.drawSlot(guiGraphics, leftPos + slot.x, topPos + slot.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        // What the slot means when empty, and the signal it gives now.
        Component target = menu.getSlot(0).hasItem() ? menu.getSlot(0).getItem().getHoverName()
                : Component.translatable("gui.colossalchests2.redstone.whole_chest");
        int textX = ContainerRedstone.TARGET_X + 22;
        guiGraphics.drawString(font, target, textX, ContainerRedstone.TARGET_Y - 1, COLOR_LABEL, false);
        Component signal = Component.translatable("gui.colossalchests2.redstone.signal", menu.getSignal());
        guiGraphics.drawString(font, signal, textX, ContainerRedstone.TARGET_Y + 9, COLOR_SIGNAL, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && menu.isGhostSlot(hoveredSlot.index) && !hoveredSlot.hasItem()) {
            String key = "gui.colossalchests2.redstone.whole_chest";
            guiGraphics.renderComponentTooltip(font, List.of(Component.translatable(key),
                    Component.translatable(key + ".info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
