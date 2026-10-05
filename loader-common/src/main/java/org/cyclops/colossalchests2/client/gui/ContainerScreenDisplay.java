package org.cyclops.colossalchests2.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.cyclops.colossalchests2.inventory.ContainerDisplay;

import java.util.List;

/**
 * Settings of a Display wall, a column per face.
 * @author rubensworks
 */
public class ContainerScreenDisplay extends AbstractContainerScreen<ContainerDisplay> {

    private static final int COLOR_LABEL = 0x404040;

    private final List<Button> faceButtons = Lists.newArrayList();
    private int shownHiddenFaces = -1;

    public ContainerScreenDisplay(ContainerDisplay menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = ContainerDisplay.WIDTH;
        this.imageHeight = ContainerDisplay.HEIGHT;
        this.inventoryLabelY = ContainerDisplay.INVENTORY_Y - 11;
    }

    @Override
    protected void init() {
        super.init();
        faceButtons.clear();
        List<Direction> faces = menu.getFaces();
        for (int i = 0; i < faces.size(); i++) {
            Direction face = faces.get(i);
            faceButtons.add(addRenderableWidget(Button.builder(getVisibilityLabel(face),
                            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, face.ordinal()))
                    .bounds(leftPos + menu.getColumnX(i) - ContainerDisplay.BUTTON_WIDTH / 2, topPos + ContainerDisplay.BUTTON_Y,
                            ContainerDisplay.BUTTON_WIDTH, 20)
                    .build()));
        }
        shownHiddenFaces = -1;
    }

    private Component getVisibilityLabel(Direction face) {
        return Component.translatable(menu.isFaceHidden(face) ? "gui.colossalchests2.display.hidden" : "gui.colossalchests2.display.shown");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // Visibility is synced after the click, so the labels follow.
        int hiddenFaces = 0;
        for (Direction face : menu.getFaces()) {
            hiddenFaces |= menu.isFaceHidden(face) ? 1 << face.ordinal() : 0;
        }
        if (hiddenFaces != shownHiddenFaces) {
            shownHiddenFaces = hiddenFaces;
            for (int i = 0; i < faceButtons.size(); i++) {
                faceButtons.get(i).setMessage(getVisibilityLabel(menu.getFaces().get(i)));
            }
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
        List<Direction> faces = menu.getFaces();
        for (int i = 0; i < faces.size(); i++) {
            Component label = Component.translatable(menu.getFaceTranslationKey(faces.get(i)));
            guiGraphics.drawString(font, label, menu.getColumnX(i) - font.width(label) / 2, ContainerDisplay.LABEL_Y, COLOR_LABEL, false);
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
        for (Button button : faceButtons) {
            if (button.isHovered()) {
                guiGraphics.renderComponentTooltip(font, List.of(Component.translatable("gui.colossalchests2.display.visibility"),
                        Component.translatable("gui.colossalchests2.display.visibility.info").withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
