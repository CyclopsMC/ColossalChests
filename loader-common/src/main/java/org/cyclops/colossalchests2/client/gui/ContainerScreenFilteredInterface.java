package org.cyclops.colossalchests2.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.cyclops.colossalchests2.blockentity.BlockEntityChestWall;
import org.cyclops.colossalchests2.capability.WallAccess;
import org.cyclops.colossalchests2.inventory.ContainerFilteredInterface;

import java.util.List;

/**
 * Settings of a Filtered Interface.
 * @author rubensworks
 */
public class ContainerScreenFilteredInterface extends AbstractContainerScreen<ContainerFilteredInterface> {

    private static final int MODE_BUTTON_X = 98;
    private static final int MODE_BUTTON_WIDTH = 70;
    private static final int COLOR_LABEL = 0x404040;

    private Button modeButton;
    private WallAccess.Mode shownMode;

    public ContainerScreenFilteredInterface(ContainerFilteredInterface menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ContainerFilteredInterface.WIDTH;
        this.imageHeight = ContainerFilteredInterface.HEIGHT;
        this.inventoryLabelY = ContainerFilteredInterface.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        modeButton = addRenderableWidget(Button.builder(getModeLabel(menu.getMode()),
                        button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ContainerFilteredInterface.BUTTON_MODE))
                .bounds(leftPos + MODE_BUTTON_X, topPos + ContainerFilteredInterface.FORM_Y - 2, MODE_BUTTON_WIDTH, 20)
                .build());
        shownMode = menu.getMode();
    }

    private static Component getModeLabel(WallAccess.Mode mode) {
        return Component.translatable(mode.getTranslationKey());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // The mode is synced after the click, so the label follows.
        if (menu.getMode() != shownMode) {
            shownMode = menu.getMode();
            modeButton.setMessage(getModeLabel(shownMode));
        }
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
        guiGraphics.drawString(font, Component.translatable("gui.colossalchests2.wall.form.label"),
                ContainerFilteredInterface.FORM_X + 22, ContainerFilteredInterface.FORM_Y + 4, COLOR_LABEL, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && ContainerFilteredInterface.isSettingsSlot(hoveredSlot.index) && !hoveredSlot.hasItem()) {
            String key = hoveredSlot.index == BlockEntityChestWall.FORM_SLOT ? "gui.colossalchests2.wall.form" : "gui.colossalchests2.wall.filter";
            guiGraphics.renderComponentTooltip(font, List.of(Component.translatable(key),
                    Component.translatable(key + ".info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        if (modeButton.isHovered()) {
            guiGraphics.renderComponentTooltip(font, List.of(Component.translatable("gui.colossalchests2.wall.mode"),
                    Component.translatable(menu.getMode().getTranslationKey() + ".info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
            return;
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
